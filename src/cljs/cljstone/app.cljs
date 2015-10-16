(ns cljstone.app
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board play-card]]
        [cljstone.dealer :only [make-random-deck]]))

(def jaina (hero/make-hero "Jaina" :mage))
(def thrall (hero/make-hero "Thrall" :shaman))

(defonce board-atom
  (-> (make-board jaina (make-random-deck) thrall (make-random-deck))
      r/atom))

(defn ^:export main []
  (html/draw-board-atom board-atom))
