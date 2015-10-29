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
  (let [is-playable? (fn [card-kw board player]
                       (let [card (cond
                                    (contains? all-spells card-kw) (-> card-kw all-spells spell->card)
                                    (contains? all-minions card-kw) (-> card-kw all-minions minion-schematic->card))]
                       (card-is-playable? card
                                          (:actual (get-mana (board player)))
                                          board
                                          player)))]
    (testing "flamecannon"
      ; the enemy player has no minions, so you can't play flamecannon.
      (is (= (is-playable? :flamecannon fresh-board :player-1)
             false))

      (let [board (play-card fresh-board :player-2 0)]
        ; if the enemy player has minions, though, you're good to go!
        (is (= (is-playable? :flamecannon board :player-1)
               true))))

    (testing "arcane intellect"
      ; not enough mana!
      (is (= (is-playable? :arcane-intellect fresh-board :player-1)
             false))

      (let [board (assoc-in fresh-board [:player-1 :mana] 3)]
        (is (= (is-playable? :arcane-intellect board :player-1)
               true))))

    (testing "arcane missiles"
      (is (= (is-playable? :arcane-missiles fresh-board :player-1))
          true))

    (testing "fireball"
      ; fireball is always castable whenever you've got the mana for it, since it can target heroes and there are always heroes.
      (is (= (is-playable? :fireball fresh-board :player-1)
             false))

      (let [board (assoc-in fresh-board [:player-1 :mana] 4)]
        (is (= (is-playable? :fireball board :player-1)
               true))))

    ; TODO implement a targetable spell that only has targets some of the time, and test it.
    ; an example is... flame lance
    ))
