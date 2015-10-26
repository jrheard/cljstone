(ns cljstone.card-test
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]])
  (:use [cljstone.bestiary :only [all-minions]]
        [cljstone.board :only [get-mana play-card]]
        [cljstone.card :only [card-is-playable?]]
        [cljstone.minion :only [minion-schematic->card]]
        [cljstone.spell :only [spell->card]]
        [cljstone.spellbook :only [all-spells]]
        [cljstone.test-helpers :only [fresh-board]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(deftest card-playability
  (let [is-playable? (fn [card board player]
                       (card-is-playable? card
                                          (:actual (get-mana (board player)))
                                          board
                                          player))]
    (testing "flamecannon"
      ; the enemy player has no minions, so you can't play flamecannon.
      (is (= (is-playable? (-> :flamecannon all-spells spell->card) fresh-board :player-1)
             false))

      (let [board (play-card fresh-board :player-2 0)]
        ; if the enemy player has minions, though, you're good to go!
        (is (= (is-playable? (-> :flamecannon all-spells spell->card) board :player-1)
               true))))))
