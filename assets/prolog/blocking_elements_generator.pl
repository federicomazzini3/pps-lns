:- use_module(library(lists)).
:- use_module(library(random)).

%range(+Min, +Max, -Num)
range(A,B,A).

range(A,B,X):-
	A2 is A+1,
    A2 < B+1,
    range(A2,B,X).

%between(+Min, +Max, -Num)
between(X,X,X).

between(A,B,X):-
	random_between(A,B,X).

%subtract(+List1,+List2,?Difference)
subtract([], _, []).

subtract([H|T], L2, L3) :-
        member(H, L2),
        !,
        subtract(T, L2, L3).

subtract([H|T1], L2, [H|T3]) :-
        subtract(T1, L2, T3).

%door_game_area(+Size, +Position, -Area)
door_game_area(N, (X,Y), A):-
	between(0,X,MINX),
    between(X,N,MAXX),
	between(0,Y,MINY),
    between(Y,N,MAXY),
    MAXX - MINX > 3,
    MAXY - MINY > 3,
	findall((P1,P2), (range(MINX,MAXX,P1),range(MINY, MAXY,P2)), A).

door_game_area(N,(X,Y),A):-
	door_game_area(N,(X,Y),A).

%game_area(+Size, +Doors, -Area)
game_area(N,[D], A):-
	door_game_area(N,D,A).

game_area(N,[D|Ds], A):-
	door_game_area(N,D,A1),
    append(A1,A2,A),
    game_area(N, Ds, A2).

%game_grid(+Size, -Area)
game_grid(N, G):-
	findall((G1, G2), (range(0, N, G1),range(0, N, G2)), G).

%game_grid(+Size, +Doors, -GridArea, -GameArea, -StoneArea)
%place_stones(8,[(8,4)],G,A,S).
place_stones(N, D, G, A, S):-
	game_grid(N,G),
	game_area(N, D, A),
    subtract(G,A,S).