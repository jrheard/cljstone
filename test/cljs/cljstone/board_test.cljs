(ns cljstone.board-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljstone.hero :as hero])
  (:use [cljstone.board :only [find-a-dead-character-in-board path-to-character make-board play-card]]
        [cljstone.character :only [get-next-character-id]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

(def hero-1 (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def hero-2 (hero/make-hero "Thrall" :shaman (get-next-character-id)))
(def board @(make-board hero-1 hero-2))

(deftest find-dead-character
  (testing "no dead characters"
    (is (= (find-a-dead-character-in-board board) nil)))

  (let [board (-> board
                  (play-card :player-1 0)
                  (assoc-in [:player-1 :minions 0 :base-health] 0))]
    (testing "one dead character"
      (is (not (= (find-a-dead-character-in-board board)
                  nil))))

    (let [board (-> board
                    (play-card :player-1 0)
                    (assoc-in [:player-1 :minions 1 :base-health] 0))
          first-minion (get-in board [:player-1 :minions 0]) ]
    ; xxx is left-to-right the correct order to seek dead minions? probably not, right?
    ; should be sorting by id, not board position - update this test when we implement deathrattles (and playing a minion at a position) and it starts mattering
    (testing "if there are two dead characters, we should get the first"
      (is (= (:base-health first-minion) 0))
      (is (= (get-in board [:player-1 :minions 1 :base-health]) 0))
      (is (= (:id (find-a-dead-character-in-board board))
             (:id first-minion)))))))

(deftest finding-paths
  (testing "looking up heroes"
    (is (= (path-to-character board (:id hero-1))
           [:player-1 :hero])
        (= (path-to-character board (:id hero-2))
           [:player-2 :hero])))

  (testing "looking up minions"
   (let [board (-> board
                   (play-card :player-1 0)
                   (play-card :player-1 0)
                   (play-card :player-2 0))
         minion-to-find (get-in board [:player-1 :minions 1])]
     (is (= (path-to-character board (:id minion-to-find))
            [:player-1 :minions 1])))))

(deftest grim-reaper
  ; TODO test for hero death

  (let [board (make-board hero-1 hero-2)]
    (swap! board play-card :player-1 0)
    (swap! board play-card :player-1 0)
    (swap! board play-card :player-1 0)
    (swap! board play-card :player-2 0)
    (swap! board play-card :player-2 0)

    (testing "minion death"
      (let [test-minion (get-in @board [:player-1 :minions 1])]
        (is (not (= (path-to-character @board (:id test-minion))
                    nil)))

        ; oh no, our minion has been attacked for its full amount of HP!
        (swap! board update-in [:player-1 :minions 1 :modifiers] conj {:type :attack
                                                                       :name nil
                                                                       :effect {:health (- (:base-health test-minion))}})

        ; the :grim-reaper watch should have automatically cleaned it up, and it should no longer exist in the board.
        ; TODO: test deathrattles.
        (is (= (path-to-character @board (:id test-minion))
               nil))))))
