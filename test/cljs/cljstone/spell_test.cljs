(ns cljstone.spell-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.spell :as s])
  (:use [schema.test :only [validate-schemas]]
        [cljstone.app :only [make-random-deck]]
        [cljstone.board :only [make-board play-card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.hero :only [make-hero]]))

(use-fixtures :once validate-schemas)

(def hero-1 (make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (make-hero "Thrall" :shaman (get-next-character-id)))
(def board (make-board hero-1 (make-random-deck) hero-2 (make-random-deck)))

(deftest flamecannon
  (let [board (-> (make-board hero-1 (make-random-deck) hero-2 (make-random-deck))
                  (play-card :player-1 0)
                  (play-card :player-2 0)
                  (play-card :player-2 0)
                  (play-card :player-2 0)
                  (assoc-in [:player-1 :hand 0]
                            (s/spell->card (s/all-spells :flamecannon))))]
    (with-redefs [rand-nth first]
      (let [board (play-card board :player-1 0)]
        (is (= (get-in board [:player-2 :minions 0 :modifiers])
               [{:type :damage-spell :name "Flamecannon" :effect {:health -4}}]))))))
