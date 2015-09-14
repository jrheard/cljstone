(ns cljstone.app
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board play-card]]
        [cljstone.combat :only [find-a-dead-character-in-board remove-minion]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.dealer :only [make-random-deck]]))

(def jaina (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (hero/make-hero "Thrall" :shaman (get-next-character-id)))

(s/defn make-board-atom :- ratom/RAtom
  [board :- Board]
  (let [board (r/atom board)]
    (add-watch board
               :grim-reaper
               (fn [_ board-atom _ new-val]
                 (when-let [dead-character (find-a-dead-character-in-board new-val)]
                   ; TODO if :character is a hero, end the game
                   ; TODO eventually program in draws if both heroes are dead
                   ; TODO if :character is a minion with a deathrattle, fire deathrattle
                   ; TODO also fire on-minion-death for flesheating ghoul, cult master, etc
                   ; anyway for right now dead-character is always a Minion
                   ; TODO have a separate kill-minion function: fires deathrattle, on-minion-death, and removes minion from board
                   ; also split this grim-reaper functionality out into something that's reusable in other places (eg cause-damage)
                   ; also will be used by twisting nether, assassinate, etc
                   (swap! board-atom remove-minion (:id dead-character)))))
    board))

(def board (let [board-atom (make-board-atom (make-board jaina (make-random-deck) thrall (make-random-deck)))
                 the-board @board-atom]
             (reset! board-atom (-> the-board
                                    (play-card :player-1 0)
                                    (play-card :player-1 0)
                                    (play-card :player-1 0)
                                    (play-card :player-2 0)
                                    (play-card :player-2 0)
                                    (play-card :player-2 0)))
             board-atom))

(defn ^:export main []
  (html/draw-board-atom board))
