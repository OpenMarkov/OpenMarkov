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
pos_x =151;
pos_y =52;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =332;
pos_y =73;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =574;
pos_y =38;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =446;
pos_y =227;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =73;
pos_y =283;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =347;
pos_y =357;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D3(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =568;
pos_y =291;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =78;
pos_y =130;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =309;
pos_y =223;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node G(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =464;
pos_y =109;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =160;
pos_y =414;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =481;
pos_y =436;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node U2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =302;
pos_y =468;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A E;

link A F;

link A U;

link B F;

link C D3;

link C G;

link D D3;

link D1 D2;

link D1 U;

link D2 D3;

link D2 U2;

link D3 U1;

link E D1;

link F D;

link F D2;

link F U2;

link G D;

link G U1;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.7 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.2 );
}

relation C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.1 );
}

relation E A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.3 0.8 0.7 );
}

relation U A D1 { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (-0.5 -2.0 -1.0 0.0 );
}

relation F A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.5 0.4 0.9 0.7 0.5 0.6 0.1 );
}

relation U2 D2 F { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 2.0 -1.0 5.0 );
}

relation D F G { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.8 0.6 0.1 0.1 0.2 0.4 0.9 );
}

relation G C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.8 0.5 0.2 );
}

relation U1 D3 G { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (-3.0 -1.0 -2.0 0.0 );
}

}
