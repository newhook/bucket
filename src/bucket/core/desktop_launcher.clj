(ns bucket.core.desktop-launcher
  (:require [bucket.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. bucket-game "bucket" 800 600)
  (Keyboard/enableRepeatEvents true))
