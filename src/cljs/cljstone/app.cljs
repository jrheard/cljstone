(ns cljstone.app
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board play-card run-continuation end-turn]]
        [cljstone.dealer :only [make-random-deck]]))

(def jaina (hero/make-hero "Jaina" :mage))
(def thrall (hero/make-hero "Thrall" :shaman))

(defonce board-atom
  (-> (make-board jaina (make-random-deck) thrall (make-random-deck))
      run-continuation
      end-turn
      r/atom))

(defn ^:export main []
  (html/draw-board-atom board-atom))
