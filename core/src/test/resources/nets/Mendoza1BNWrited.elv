//	   Network
//	   Elvira format

bnet "bayesnet" {

//		 Network Properties

kindofgraph = "directed";
title = "Test bayes net";
author = "jmendoza";
default node states = ("presente" , "ausente");

// Variables

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =47;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =178;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =304;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =110;
pos_y =145;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =241;
pos_y =143;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =178;
pos_y =247;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

//		 Links of the associated graph:

link A B;
link A D;
link B C;
link B D;
link B E;
link C E;
link D E;
link D F;
link E F;
//		Network Relationships:

relation A {
kind-of-relation = potential;
deterministic=false;
values = table(0.8 0.2 );
}

relation B A {
kind-of-relation = potential;
deterministic=false;
values = table(0.1 0.4 0.9 0.6 );
}

relation C B {
deterministic=false;
values = table(0.5 0.5 0.5 0.5 );
}

relation D A B {
kind-of-relation = potential;
deterministic=false;
values = table(0.1 0.2 0.3 0.4 0.9 0.8 0.7 0.6 );
}

relation E B C D {
deterministic=false;
values = table(0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation F D E {
deterministic=false;
values = table(0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

}

