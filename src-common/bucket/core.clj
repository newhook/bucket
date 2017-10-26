(ns bucket.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.math :refer :all]
            [play-clj.g2d :refer :all]))

(defn create-raindrop []
  (assoc (texture "droplet.png")
         :x (rand (- 800 64))
         :y 480
         :width 64
         :height 64
         :raindrop? true))

(defn overlaps [r1 r2]
  (and
    (< (:x r1) (+ (:x r2) (:width r2)))
    (> (+ (:x r1) (:width r1)) (:x r2))
    (< (:y r1) (+ (:y r2) (:height r2)))
    (> (+ (:y r1) (:height r1)) (:y r2))))

(defn get-velocity
  [entities {:keys [bucket? raindrop?] :as entity}]
  (cond
    bucket? (cond
              (key-pressed? :left) [ -200  0]
              (key-pressed? :right) [ 200 0]
              :else [0 0])
    raindrop? [0 -200]
    :else [0 0]))

(defn move [{:keys [delta-time]} entities {:keys [x y] :as entity}]
  (let [[x-velocity y-velocity] (get-velocity entities entity)
        x-change (* x-velocity delta-time)
        y-change (* y-velocity delta-time)]
    (cond
      (or (not= 0 x-change) (not= 0 y-change))
      (assoc entity
        :x-velocity x-velocity
        :y-velocity y-velocity
        :x-change x-change
        :y-change y-change
        :x (+ x x-change)
        :y (+ y y-change))
      :else
      entity)))

(defn invalid-location?
  [screen entities entity]
  (or (< (:x entity) 0) (> (:x entity) 800) (< (:y entity) 0)))

(defn prevent-move
  [screen entities {:keys [x y x-change y-change] :as entity}]
  (let [old-x (- x x-change)
        old-y (- y y-change)]
    (merge entity
           (when (invalid-location? screen entities entity)
             {:x-velocity 0 :x-change 0 :x old-x}))))

(defn capture [screen entities]
  (let [bucket (first entities)
        touched (filter #(overlaps bucket %) (rest entities))]
    (remove (set touched) entities)))

(defscreen main-screen
           :on-show
           (fn [screen entities]
             (println "on-show")
             (add-timer! screen :spawn-raindrop 1 1)
             (update! screen :renderer (stage))
             (assoc (texture "bucket.png")
               :x (- (/ 800 2) (/ 64 2))
               :y 20
               :width 64
               :height 64
               :bucket? true))

           :on-timer
           (fn [screen entities]
             (case (:id screen)
               :spawn-raindrop (conj entities (create-raindrop))
               nil))
           :on-render
           (fn [screen entities]
             (clear!)
             ;(cond
             ;  (key-pressed? :up) (remove-timer! screen :spawn-raindrop)
             ;  (key-pressed? :down) (add-timer! screen :spawn-raindrop 1 1))
             (->> entities
                  (map (fn [entity]
                         (->> entity
                              (move screen entities)
                              (prevent-move screen entities))))
               (capture screen)
               (render! screen))))

(defgame bucket-game
         :on-create
         (fn [this]
           (set-screen! this main-screen)))

;; For working with the repl.
(defscreen blank-screen
           :on-render
           (fn [screen entities]
             (clear!)))

(set-screen-wrapper!
  (fn [screen screen-fn]
    (try (screen-fn)
      (catch Exception e
        (.printStackTrace e)
        (set-screen! bucket-game blank-screen)))))

;; restart the main screen
(comment
  (in-ns 'bucket.core)
  (play-clj.core/on-gl (set-screen! bucket-game main-screen)))