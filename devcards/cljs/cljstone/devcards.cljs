(ns cljstone.devcards
  (:require-macros [devcards.core :as dc :refer [defcard]])
  (:use [cljstone.html :only [draw-end-turn-button]])
  )





(defcard end-turn-card
  [:div "hello world"]
  )
