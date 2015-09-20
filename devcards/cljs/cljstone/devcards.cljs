(ns cljstone.devcards
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :as dc :refer [defcard]])
  (:use [cljs.core.async :only [chan put!]]
        [cljstone.board :only [make-board play-card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.dealer :only [make-random-deck]]
        [cljstone.hero :only [make-hero]]
        [cljstone.html :only [draw-combat-log draw-end-turn-button handle-game-events]]))

(defonce jaina (make-hero "Jaina" :mage (get-next-character-id)))
(defonce thrall (make-hero "Thrall" :shaman (get-next-character-id)))

(defonce board (-> (make-board jaina (make-random-deck) thrall (make-random-deck))
                   (play-card :player-1 0)
                   (play-card :player-1 0)
                   (play-card :player-2 0)
                   (play-card :player-2 0)))

(defonce board-atom (r/atom board))

(defonce game-state {:board-atom board-atom
                      :game-event-chan (chan)
                      :mouse-event-chan (chan)})

(defonce game-event-handler (handle-game-events game-state))

(defcard end-turn-card
  "Ends the current turn."
  (dc/reagent
    (fn [_ _]
      [:div
       [:div (str "Whose turn: "(:whose-turn @board-atom))]
       [:div (str "Turn number: "(:turn @board-atom))]
       [draw-end-turn-button @board-atom game-state]])))

(defonce an-attack
  (put! (:game-event-chan game-state)
        {:type :attack
         :origin-id (get-in board [:player-1 :minions 0 :id])
         :destination-id (get-in board [:player-2 :minions 0 :id])}))

(defcard combat-log-card
  "A log of what's happened combat-wise."
  (dc/reagent
    (fn [_ _]
      [:div {:style {:position "relative"
                     :overflow "hidden"
                     :height "50px"
                     :padding "5px"
                     :border "solid 1px black"}}
       [draw-combat-log @board-atom]]))
  )

(defcard board
  (-> @board-atom
      (update-in [:player-1] dissoc :deck)
      (update-in [:player-1] dissoc :hand)
      (update-in [:player-2] dissoc :deck)
      (update-in [:player-2] dissoc :hand)
      )
  )
