// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =344;
pos_y =156;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =651;
pos_y =228;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D3(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =986;
pos_y =177;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =184;
pos_y =77;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =181;
pos_y =201;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =410;
pos_y =345;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =473;
pos_y =45;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =816;
pos_y =179;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node G(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1061;
pos_y =313;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node H(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =672;
pos_y =51;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =658;
pos_y =407;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A D1;

link B D1;

link C E;

link C H;

link C U;

link D1 C;

link D2 F;

link D2 U;

link D3 G;

link E D2;

link F D3;

link G U;

link H F;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.6 0.4 );
}

relation B { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation C D1 { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation E C { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation G D3 { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation H C { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation F D2 H { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation U C D2 G { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 );
}

}
