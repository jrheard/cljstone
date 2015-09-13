(ns cljstone.test-helpers
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.board :only [make-board play-card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.dealer :only [make-random-deck]]
        [cljstone.hero :only [make-hero]]))

(use-fixtures :once validate-schemas)

(def hero-1 (make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (make-hero "Thrall" :shaman (get-next-character-id)))

(def fresh-board (make-board hero-1 (make-random-deck) hero-2 (make-random-deck)))

(def three-minions-per-player-board
  (-> fresh-board
      (play-card :player-1 0)
      (play-card :player-1 0)
      (play-card :player-2 0)
      (play-card :player-2 0)
      (play-card :player-2 0)
      (play-card :player-1 0)))
