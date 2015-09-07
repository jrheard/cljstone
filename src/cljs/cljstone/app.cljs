(ns cljstone.app
  (:require [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board play-card]]
        [cljstone.card :only [Card]]
        [cljstone.character :only [get-next-character-id]]
        [cljstone.minion :only [all-minions minion-schematic->card]]
        [cljstone.spell :only [Spell all-spells spell->card]]))

(def NUM-CARDS-IN-DECK 30)

(def jaina (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (hero/make-hero "Thrall" :shaman (get-next-character-id)))

; XXX perhaps a dealer.cljs that handles spinning up a deck?
(s/defn make-random-deck :- [Card]
  []
  (assoc (mapv minion-schematic->card
              (repeatedly NUM-CARDS-IN-DECK #(rand-nth (vals all-minions))))
         4
         (spell->card (all-spells :flamecannon))))

(def board (let [board-atom (make-board jaina (make-random-deck) thrall (make-random-deck))
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
  (html/mount-reagent board))
