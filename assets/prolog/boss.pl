:- use_module(library(lists)).
:- use_module(library(random)).

% Check life level for Boss or Character, low if life is under 50%
% @Who is decomposed terms to take only life and maxLife
% -low|high
checkLife(Who, low) :- Who =.. [_,_,_,Life,MaxLife], X is MaxLife / 2, Life < X , !.
checkLife(Who, high).

% List of probability value for actions based on Boss and Character life level
% @high|low (Boss)
% @high|low (Character)
% -Probability List[Attack, Defense, Move]
getActionsProbability(high, high, [0.7, 0.0, 1.0]).
getActionsProbability(high, low,  [0.5, 0.0, 1.0]).
getActionsProbability(low,  high, [0.7, 1.0, 0.0]).
getActionsProbability(low,  low,  [0.5, 0.7, 1.0]).

% Random Action type based on probability list
% @List of probability value for actions
% -attack|defence|move
randomActionType([X,_,_], attack)    :- maybe(X), !.
randomActionType([_,X,_], defence)   :- maybe(X), !.
randomActionType([_,_,_], move).

% ******
% ATTACK
% ******
% Concrate attack action
% @Boss
% @Character
% -attack1(direction)|attack2|attack3
attackAction(Boss, Character, attack1(right)) :-
	Boss =.. [_,BossX,BossY,_,_],
    Character =.. [_,CharacterX,CharacterY,_,_],
    BossX is CharacterX,
    BossY < CharacterY, !.
attackAction(Boss, Character, attack1(left)) :-
	Boss =.. [_,BossX,BossY,_,_],
    Character =.. [_,CharacterX,CharacterY,_,_],
    BossX is CharacterX,
    BossY > CharacterY, !.
attackAction(Boss, Character, attack1(down)) :-
	Boss =.. [_,BossX,BossY,_,_],
    Character =.. [_,CharacterX,CharacterY,_,_],
    BossY is CharacterY,
    BossX < CharacterX, !.
attackAction(Boss, Character, attack1(top)) :-
	Boss =.. [_,BossX,BossY,_,_],
    Character =.. [_,CharacterX,CharacterY,_,_],
    BossY is CharacterY,
    BossX > CharacterX.
attackAction(_, _, attack2) :- maybe(0.5), !.
attackAction(_, _, attack3).

% ******
% DEFENCE
% ******
% Concrete defence action
% Teleport the boss to the mirror position of the current and check if he is available
% @Boss
% @Character
% @Room
% @Blocks
% -defence(x,y)
defenceAction(boss(X, Y, _, _), _, room(RX, RY), Blocks, defence(NX, NY)) :-
	SX is RX - X, SY is RY - Y, getFreePlace(room(RX, RY), Blocks, block(SX, SY), block(NX, NY)).

% Check if position is available excluding blocking elements
% @Blocks (es: List[block(X,Y),...])
% @P block(X,Y)
checkPosition([],_).
checkPosition([H|T], P) :- H\==P, checkPosition(T, P).

% Check if X, Y are inside room
% @Room
% @X
% @Y
checkRoom(room(RX, RY), X, Y) :- X=<RX, Y=<RY, X>=0, Y>=0.

% Get new free place X, Y free around block
% @Room
% @Blocks
% @block(X,Y) (origin block)
% @Distance
% -block(X,Y) (new free block)

% (X+1, Y)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, BY)) :- X is BX + Distance, checkRoom(Room, X, BY), checkPosition(Blocks, block(X, BY)).
% (X+1, Y+1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, Y)) :- X is BX + Distance, Y is BY + Distance, checkRoom(Room, X, Y), checkPosition(Blocks, block(X, Y)).
% (X+1, Y-1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, Y)) :- X is BX + Distance, Y is BY - Distance, checkRoom(Room, X, Y), checkPosition(Blocks, block(X, Y)).
% (X-1, Y)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, BY)) :- X is BX - Distance, checkRoom(Room, X, BY), checkPosition(Blocks, block(X, BY)).
% (X-1, Y+1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, Y)) :- X is BX - Distance, Y is BY + Distance, checkRoom(Room, X, Y), checkPosition(Blocks, block(X ,Y)).
% (X-1, Y-1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(X, Y)) :- X is BX - Distance, Y is BY - Distance, checkRoom(Room, X, Y), checkPosition(Blocks, block(X, Y)).
% (X, Y+1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(BX, Y)) :- Y is BY + Distance, checkRoom(Room, BX, Y), checkPosition(Blocks, block(BX, Y)).
% (X, Y-1)
freePlace(Room, Blocks, block(BX, BY), Distance, block(BX, Y)) :- Y is BY - Distance, checkRoom(Room, BX, Y), checkPosition(Blocks, block(BX, Y)).

% Get first available free place starting from Point,
% if is not free, search new point at some distance and increase it until found
% @Room
% @Blocks
% @P
% @Distance
% -NEW
getFreePlace(_, Blocks, P, P) :- checkPosition(Blocks, P), !.
getFreePlace(Room, Blocks, P, NEW) :- getFreePlace(Room, Blocks, P, 1, NEW).
getFreePlace(Room, Blocks, P, Distance, NEW) :- freePlace(Room, Blocks, P, Distance ,NEW), !.
getFreePlace(Room, Blocks, P, Distance, NEW) :- NewDistance is Distance + 1, getFreePlace(Room, Blocks, P, NewDistance ,NEW).

% ******
% MOVE
% ******
% Concrete move action
% Move to character's position
% @Boss
% @Character
% @Room
% @Blocks
% -move(x,y)
moveAction(_, character(X, Y, _, _), _, _, move(X,Y)).

% Concrete action by type
% @Boss
% @Character
% @Room
% @Blocks
% @attack|defence|move
% -action
action(Boss, Character, _, _, attack, Action) :-
	attackAction(Boss, Character, Action).
action(Boss, Character, Room, Blocks, defence, Action) :-
	defenceAction(Boss, Character, Room, Blocks, Action).
action(Boss, Character, Room, Blocks, move, Action) :-
	moveAction(Boss, Character, Room, Blocks, Action).

% *********
% MAIN GOAL
% *********
% @boss(x, y, life, maxLife)
% @character(x, y, life, maxLife)
% @room(size, size)
% @list[block(x, y)]
% -action
behaviour(Boss, Character, Room, Blocks, Action) :-
		checkLife(Boss, BossLife),
    	checkLife(Character, CharacterLife),
        getActionsProbability(BossLife, CharacterLife, ActionProbability),
        randomActionType(ActionProbability, ActionType),
        action(Boss, Character, Room, Blocks, ActionType, Action).
behaviour(Boss, Character, Room, Blocks, Action) :-
		behaviour(Boss, Character, Room, Blocks, Action).

% TEST GOAL
% behaviour(boss(1,1,4,10),character(1,4,10,10),room(9,9),[block(5,5),block(5,6)],A).

