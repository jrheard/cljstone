(ns cljstone.app-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]])
  (:use [cljstone.board :only [path-to-character play-card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.hero :only [make-hero]]
        [cljstone.test-helpers :only [fresh-board]]
        [schema.test :only [validate-schemas]]))

(use-fixtures :once validate-schemas)

#_(deftest grim-reaper
  ; TODO test for hero death

  (let [board (make-board-atom fresh-board)]
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

