(ns cljstone.app
  (:require [reagent.core :as r]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board summon-minion]]))

(def next-character-id (atom 0))

(defn get-next-character-id []
  (let [id-to-return @next-character-id]
    (swap! next-character-id inc)
    id-to-return))

(def jaina (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (hero/make-hero "Thrall" :shaman (get-next-character-id)))

; TODO ok next up: cards, hands, decks
; TODO boy this toy init function is getting messy, clean it up in the next pass when we start getting randomly generated hands/decks/etc
(def board (let [board-atom (make-board jaina thrall)
                 the-board @board-atom]
             (reset! board-atom (-> the-board
                                    (summon-minion :half-1 (:chillwind-yeti minion/neutral-minions) (get-next-character-id))
                                    (summon-minion :half-2 (:magma-rager minion/neutral-minions) (get-next-character-id))
                                    (summon-minion :half-2 (:bloodfen-raptor minion/neutral-minions) (get-next-character-id))
                                    (summon-minion :half-2 (:wisp minion/neutral-minions) (get-next-character-id))
                                    (summon-minion :half-1 (:goldshire-footman minion/neutral-minions) (get-next-character-id))))
             board-atom))

(html/mount-reagent board)
