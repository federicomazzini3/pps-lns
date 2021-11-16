:- use_module(library(lists)).
:- use_module(library(random)).

% dropFirst(?Elem,?List,?OutList)
dropFirst(_,[],[]):- !.
dropFirst(X,[X|T],T):- !.
dropFirst(X,[H|Xs],[H|L]):- dropFirst(X,Xs,L).

% data una room in RX RY ottiene i possibili spazi liberi in termini di coordinate X Y intorno, in base alle room presenti in lista L
freePlace(L,RX,Y,X,Y):- X is RX+1, \+(member(room(X,Y,_),L)).
freePlace(L,RX,Y,X,Y):- X is RX-1, \+(member(room(X,Y,_),L)).
freePlace(L,X,RY,X,Y):- Y is RY+1, \+(member(room(X,Y,_),L)).
freePlace(L,X,RY,X,Y):- Y is RY-1, \+(member(room(X,Y,_),L)).

% addFreePlaces(+L,+P,+RX,+RY,-OP)
% aggiunge alla lista dei Places liberi, quelli relativi ad una nuova Room
addFreePlaces(L,P,RX,RY,OP):- findall(p(X,Y),freePlace(L,RX,RY,X,Y),P2), append(P,P2,P3), list_to_set(P3,OP).

% placeRoom(+L,+P,+RX,+RY,+RT,-OL,-OP)
placeRoom(L,P,RX,RY,RT,[room(RX,RY,RT)|L],OP):- dropFirst(p(RX,RY),P,P2), addFreePlaces(L,P2,RX,RY,OP).

% autoPlaceRoom(+L,+P,+RT,-OL,-OP)
autoPlaceRoom(L,P,RT,OL,OP):- random_member(p(X,Y),P), placeRoom(L,P,X,Y,RT,OL,OP).

% placeStart(-L,-P)
placeStart(L,P):- placeRoom([],[],0,0,s,L,P).

% isValidBossPlace(+L,+P)
isValidBossPlace(L,p(X,Y)) :- findall(p,freePlace(L,X,Y,_,_),P), length(P,C), C==3.

% filterBossPlaces(+L,+P,?BP)
filterBossPlaces(L,[],[]).
filterBossPlaces(L,[H|T],[H|BP]) :- isValidBossPlace(L,H), !, filterBossPlaces(L,T,BP).
filterBossPlaces(L,[_|T],BP) :- filterBossPlaces(L,T,BP).

% placeBoss(+L,+P,-OL)
placeBoss(L,P,OL):- filterBossPlaces(L,P,BP), autoPlaceRoom(L,BP,b,OL,_).

% randomType(-RT)
randomType(i):- maybe(0.15), !.
randomType(a):- maybe(0.75), !.
randomType(e).

% generate(+N,+L,+P,-OL,-OP)
generate(0,L,P,L,P):- !.
generate(N,L,P,OL,OP):- N2 is N-1,N2>=0,
				  		randomType(RT),
                        autoPlaceRoom(L,P,RT,L2,P2),
				  		generate(N2,L2,P2,OL,OP).

% generateDungeon(+N,-L)
generateDungeon(N,L):- placeStart(L0,P0), generate(N,L0,P0,L1,P1), placeBoss(L1,P1,L).