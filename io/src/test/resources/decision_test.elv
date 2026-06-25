// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node Disease_X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =107;
pos_y =47;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Result_Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =288;
pos_y =132;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("not performed" "positive" "negative");
}

node Test_T(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =462;
pos_y =37;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Treatment_D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =284;
pos_y =241;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Quality_of_life_U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =107;
pos_y =326;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Cost_of_test_U2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =464;
pos_y =324;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Global_utility_U0(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =277;
pos_y =418;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link Cost_of_test_U2 Global_utility_U0;

link Disease_X Quality_of_life_U1;

link Disease_X Result_Y;

link Quality_of_life_U1 Global_utility_U0;

link Result_Y Treatment_D;

link Test_T Cost_of_test_U2;

link Test_T Result_Y;

link Treatment_D Quality_of_life_U1;

//Network Relationships: 

relation Disease_X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.07 0.93 );
}

relation Result_Y Disease_X Test_T { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.0 1.0 0.0 1.0 0.91 0.0 0.03 0.0 0.09 0.0 0.97 0.0 );
}

relation Quality_of_life_U1 Disease_X Treatment_D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= generalizedTable (80.0|range(70.0,90.0) 30.0 90.0 100.0 );
}

relation Cost_of_test_U2 Test_T { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= generalizedTable (-2.0 0.0 );
}

relation Global_utility_U0 Cost_of_test_U2 Quality_of_life_U1 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
