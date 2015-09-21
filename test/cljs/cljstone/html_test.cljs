(ns cljstone.html-test
  (:require [cljs.test :refer-macros [async deftest is use-fixtures]]
            [schema.core :as s :refer-macros [with-fn-validation]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use [cljs.core.async :only [chan <! >! put!]]
        [clojure.string :only [trim]]
        [cljstone.html :only [draw-card draw-minion-card]]
        [cljstone.test-helpers :only [boulderfist-card fresh-board]]))

(def test-board (-> fresh-board
                    (assoc :whose-turn :player-2)
                    (assoc-in [:player-2 :hand 0] boulderfist-card)))

(deftest drawing-cards
  (with-fn-validation
    (let [game-event-chan (chan)
          card (draw-card boulderfist-card 0 :player-2 true game-event-chan)
          props (nth card 1)]
      (is (= (trim (props :class)) "card neutral minion playable"))
      (is (= (props :data-card-index) 0))
      (is (= (nth card 2) [draw-minion-card boulderfist-card]))

      ; fire a click event, test to see if something was put in game-event-chan
      ((props :on-click) 'foo)

      (async done
         (go
           (is (= (<! game-event-chan)
                  {:type :play-card
                   :player :player-2
                   :index 0}))
           (done))))))
