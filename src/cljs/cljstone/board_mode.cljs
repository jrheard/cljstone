(ns cljstone.board-mode
  (:require [schema.core :as s])
  (:use [cljstone.card :only [Card]]
        [cljstone.character :only [Character Player]]
        [cljstone.minion :only [Minion]]))

;; Board modes.
;
; Much of the game is spent in DefaultMode - in this mode, you can play cards, attack minions,
; press "end turn", etc.
;
; Frequently, though, we'll need to enter an intermediate state, like
; "The player's playing a minion with a targetable battlecry - let's show them which characters can
; be targeted ***and wait for them to choose one***." The same thing happens when the player's played
; one of the druid class's choose-one cards, or when we're performing the initial mulligan, etc.
;
; In situations like this, we represent that intermediate state by e.g. putting a TargetingMode
; on the board's :mode key. This lets us draw the board in a way that makes it clear to the player
; what sort of input we're waiting for. Once we *receive* that input, we call the mode's :continuation
; function and pass that input along.
;
; For instance, if the player's just chosen a target for a minion's battlecry, the mode's continuation
; function will e.g. cast the battlecry on the chosen target, summon the minion in the correct position,
; and remove the minion's card from the player's hand. That's what continuation functions are for.
; This scheme lets us say "ok we're in an intermediate state, let's get user input!"; block until we've
; received that input; and then call a function that continues the flow of the game.

(s/defschema DefaultMode
  {:type :default})

(s/defschema PositioningMode
  {:type :positioning
   :minion Minion
   :continuation s/Any})

(s/defschema TargetingMode
  {:type :targeting
   :targets [Character]
   (s/optional-key :attacker) Character
   ; choose-one mode will also have an optional positioning-info
   ; this is because minions can be positioned and can then have targetable battlecries or choose-ones
   (s/optional-key :positioning-info) {:minion Minion
                                       :index s/Int}
   :continuation s/Any})

(s/defschema MulliganCard
  {:card Card
   :selected s/Bool})

(s/defschema MulliganMode
  {:type :mulligan
   :cards [MulliganCard]
   :continuation s/Any})

(s/defschema GameOverMode
  {:type :game-over
   :winner Player})

(s/defschema BoardMode
  ; unfortunately, it's hard to encode this with schema. s/enum is only for literals,
  ; abstract-map-schema is experimental and i haven't gotten it to work here,
  ; and i'm not sure how to express this with s/conditional.
  ; so anyway these modes are currently just for documentation.
  s/Any
  #_(s/enum
    DefaultMode
    PositioningMode
    TargetingMode
    MulliganMode))
