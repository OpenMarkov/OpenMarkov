// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node N2_N3(finite-states) {
title = "N2-N3";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =70;
pos_y =27;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("N2/N3 positivo" "N2/N3 negativo");
}

node TAC(finite-states) {
comment = "Resultado del TAC";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =122;
pos_y =96;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("positivo" "negativo");
}

node Tratamiento(finite-states) {
comment = "Tratamiento final";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =320;
pos_y =347;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("toracotomía" "pqt-rt" "paliativo");
}

node MED(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =532;
pos_y =96;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("positivo" "negativo" "no-realizada");
}

node Dec:MED(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =415;
pos_y =161;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node U(continuous) {
title = "Sv MED";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =469;
pos_y =421;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U1(continuous) {
title = "Vida-Salud sv";
comment = "Cantidad y calidad de vida 
para los pacientes que 
sobreviven a los
tratamientos y a las pruebas
";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =160;
pos_y =424;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
unit = "QUALYs";
}

node U2(continuous) {
title = "Sv inmediata tto";
comment = "Probabilidad de sobrevivir 
al tratamiento";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =321;
pos_y =421;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U3(continuous) {
title = "VS 1";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =315;
pos_y =510;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U4(continuous) {
title = "Mb MED";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =631;
pos_y =480;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
unit = "QUALYs";
}

node Sv_MED(finite-states) {
title = "Sv MED";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =440;
pos_y =285;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node PTB(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =315;
pos_y =99;
relevance = 7.0;
purpose = "Prueba";
num-states = 3;
states = ("positivo" "negativo" "no realizada");
}

node Dec:PTB(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =229;
pos_y =158;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node Mb_PTB(continuous) {
title = "Mb PTB";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =46;
pos_y =488;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U5(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =310;
pos_y =622;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

// Links of the associated graph:

link N2_N3 TAC;

link TAC Tratamiento;

link MED Tratamiento;

link Dec:MED MED;

link N2_N3 MED;

link Dec:MED Tratamiento;

link TAC Dec:MED;

link U1 U3;

link U2 U3;

link Tratamiento U2;

link N2_N3 U1;

link Tratamiento U1;

link U U3;

link Dec:MED Sv_MED;

link Sv_MED U;

link Sv_MED U4;

link Dec:PTB PTB;

link TAC PTB;

link N2_N3 PTB;

link Dec:PTB Mb_PTB;

link TAC Dec:PTB;

link Dec:PTB Tratamiento;

link Dec:PTB Dec:MED;

link PTB Dec:MED;

link PTB Tratamiento;

link Mb_PTB U5;

link U3 U5;

link U4 U5;

//Network Relationships: 

relation N2_N3 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.45 0.55 );
}

relation TAC N2_N3 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.67 0.3 0.33 0.7 );
}

relation MED Dec:MED N2_N3 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.83 0.0 0.0 0.0 0.17 1.0 0.0 0.0 0.0 0.0 1.0 1.0 );
}

relation U2 Tratamiento { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.95 0.99 1.0 );
}

relation U1 N2_N3 Tratamiento { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.66 0.83 0.5 3.0 2.0 1.25 );
}

relation U3 U1 U2 U { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation Sv_MED Dec:MED { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.995 1.0 0.0050 0.0 );
}

relation U Sv_MED { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 0.0 );
}

relation U4 Sv_MED { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-0.1 0.0 );
}

relation PTB Dec:PTB TAC N2_N3 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.52 0.12 0.15 0.03 0.0 0.0 0.0 0.0 0.48 0.88 0.85 0.97 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 );
}

relation Mb_PTB Dec:PTB { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-0.0 0.0 );
}

relation U5 Mb_PTB U3 U4 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
