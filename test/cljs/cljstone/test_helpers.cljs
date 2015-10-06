(ns cljstone.test-helpers
  (:require [schema.core :as s]
            [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.bestiary :only [all-minions]]
        [cljstone.board :only [make-board play-card]]
        [cljstone.card :only [Card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.dealer :only [make-random-deck]]
        [cljstone.hero :only [make-hero]]
        [cljstone.minion :only [Minion make-minion minion-schematic->card]]))

(use-fixtures :once validate-schemas)

(def hero-1 (make-hero "Jaina" :mage))
(def hero-2 (make-hero "Thrall" :shaman))

(def fresh-board (make-board hero-1 (make-random-deck) hero-2 (make-random-deck)))

(s/defn get-minion-card :- Card
  [minion-keyword]
  (-> all-minions minion-keyword minion-schematic->card))

(s/defn get-minion :- Minion
  [minion-keyword]
  (make-minion (all-minions minion-keyword) (get-next-character-id)))

(def boulderfist-card (-> all-minions :boulderfist-ogre minion-schematic->card))
(def boulderfist-minion (-> all-minions :boulderfist-ogre (make-minion 12345)))

(def goldshire-card (-> all-minions :goldshire-footman minion-schematic->card))

(def three-minions-per-player-board
  (-> fresh-board
      (play-card :player-1 0)
      (play-card :player-1 0)
      (play-card :player-2 0)
      (play-card :player-2 0)
      (play-card :player-2 0)
      (play-card :player-1 0)))
