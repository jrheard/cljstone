(ns cljstone.devcards
  (:require [reagent.core :as r])
  (:require-macros [devcards.core :as dc :refer [defcard]])
  (:use [cljs.core.async :only [chan]]
        [cljstone.board :only [make-board]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.hero :only [make-hero]]
        [cljstone.html :only [draw-end-turn-button handle-game-events]]))

(def jaina (make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (make-hero "Thrall" :shaman (get-next-character-id)))

(defonce board (make-board jaina [] thrall []))
(defonce board-atom (r/atom board))

(defonce game-state {:board-atom board-atom
                      :game-event-chan (chan)
                      :mouse-event-chan (chan)})

(defonce game-event-handler (handle-game-events game-state))

(defcard end-turn-card
  "Ends the current turn. Hover over the button to see whose+which turn it is."
  (dc/reagent
    (fn [_ _]
      [draw-end-turn-button @board-atom game-state])))
