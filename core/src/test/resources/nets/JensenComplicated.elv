// Influence Diagram
//   Elvira format 

idiagram  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =64;
pos_y =70;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =66;
pos_y =328;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =258;
pos_y =219;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =253;
pos_y =441;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =390;
pos_y =320;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =396;
pos_y =596;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node G(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =535;
pos_y =194;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node H(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =769;
pos_y =414;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node I(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =793;
pos_y =262;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node J(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =994;
pos_y =385;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node K(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1015;
pos_y =639;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node L(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1002;
pos_y =138;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =53;
pos_y =588;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =541;
pos_y =466;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D3(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =729;
pos_y =727;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D4(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =732;
pos_y =100;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =1189;
pos_y =373;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A C;

link B C;

link B D;

link B D1;

link C E;

link D E;

link D F;

link D1 D;

link D1 U;

link D2 D3;

link D2 I;

link D3 D4;

link D3 K;

link D4 L;

link E D2;

link E G;

link F D2;

link F H;

link G D4;

link G I;

link H J;

link H K;

link I L;

link J U;

link K U;

link L U;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.2 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.1 );
}

relation C A B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation D B D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.2 0.3 0.5 0.9 0.8 0.7 0.5 );
}

relation E C D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.5 0.2 0.6 0.1 0.5 0.8 0.4 );
}

relation G E { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 0.9 0.1 );
}

relation F D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.4 0.8 0.6 0.2 );
}

relation I D2 G { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.4 0.6 0.8 0.8 0.6 0.4 0.2 );
}

relation H F { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.9 0.7 0.1 );
}

relation L D4 I { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.0 0.8 0.2 0.9 1.0 0.2 0.8 );
}

relation J H { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.6 0.7 0.4 0.3 );
}

relation U D1 J K L { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (15.0 10.0 5.0 0.0 3.0 6.0 9.0 1.0 2.0 6.0 8.0 11.0 4.0 0.0 6.0 8.0 );
}

relation K D3 H { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.7 0.9 1.0 0.8 0.3 0.1 0.0 );
}

}
