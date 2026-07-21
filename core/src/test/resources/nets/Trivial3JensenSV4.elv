// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =328;
pos_y =142;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =642;
pos_y =142;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node T(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =642;
pos_y =303;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =643;
pos_y =467;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =145;
pos_y =317;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("maybe" "yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =421;
pos_y =415;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =217;
pos_y =419;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =460;
pos_y =494;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U3(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =314;
pos_y =496;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U4(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =383;
pos_y =579;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A B;

link A T;

link A U;

link B T;

link D1 A;

link D1 U1;

link D2 U;

link D2 U2;

link T D2;

link U U3;

link U1 U3;

link U2 U4;

link U3 U4;

//Network Relationships: 

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.95 0.7 0.05 );
}

relation T A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.2 0.3 0.4 0.9 0.8 0.7 0.6 );
}

relation A D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.6 0.3 0.5 0.4 0.7 );
}

relation U A D2 { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (2.0 1.0 9.0 10.0 );
}

relation U1 D1 { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (30.0 50.0 20.0 );
}

relation U2 D2 { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (3.0 5.0 );
}

relation U3 U U1 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation U4 U2 U3 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

}
