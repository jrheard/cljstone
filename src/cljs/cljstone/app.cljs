(ns cljstone.app
  (:require [reagent.core :as r]
            [schema.core :as s]
            [cljstone.minion :as minion]
            [cljstone.hero :as hero]
            [cljstone.html :as html])
  (:use [cljstone.board :only [Board make-board play-card]]
        [cljstone.character :only [get-next-character-id]]))

(def jaina (hero/make-hero "Jaina" :mage (get-next-character-id)))
(def thrall (hero/make-hero "Thrall" :shaman (get-next-character-id)))

(def board (let [board-atom (make-board jaina thrall)
                 the-board @board-atom]
             (reset! board-atom (-> the-board
                                    (play-card :player-1 0)
                                    (play-card :player-1 0)
                                    (play-card :player-1 0)
                                    (play-card :player-2 0)
                                    (play-card :player-2 0)
                                    (play-card :player-2 0)))
             board-atom))


; xxx global side effect
;(html/mount-reagent board)
