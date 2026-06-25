// Influence Diagram
//   Elvira format 

idiagram  Untitled1 { 

// Network Properties

visualprecision = "0.00";
version = 1.0;
default node states = (present , absent);

// Network Variables 

node Sonda(finite-states) {
title = "Sonda uretral";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =234;
pos_y =55;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (si no);
}

node Infección(finite-states) {
title = "Infección inicio";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =229;
pos_y =160;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (gram_positivo gram_negativo levaduras ninguno);
}

node Infección_2(finite-states) {
title = "Infección 48 h";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =667;
pos_y =163;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (gram_positivo gram_negativo levaduras ninguno);
}

node D(finite-states) {
title = "Fiebre";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =98;
pos_y =279;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node E(finite-states) {
title = "Malestar general";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =157;
pos_y =354;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node F(finite-states) {
title = "Molestias orinar";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =246;
pos_y =298;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node G(finite-states) {
title = "S. confusional";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =313;
pos_y =353;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node D_1(finite-states) {
title = "Fiebre 48 h";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =586;
pos_y =286;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node E_1(finite-states) {
title = "Mal. general 48 h";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =594;
pos_y =399;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node F_1(finite-states) {
title = "Mol. orinar 48 h";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =693;
pos_y =301;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node G_1(finite-states) {
title = "S. conf. 48 h";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =693;
pos_y =359;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

node D4(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =442;
pos_y =265;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (present absent);
}

// links of the associated graph:

link Sonda Infección;

link Infección D;

link Infección E;

link Infección F;

link Infección G;

link Infección_2 D_1;

link Infección_2 E_1;

link Infección_2 F_1;

link Infección_2 G_1;

link Infección Infección_2;

link D D4;

link F D4;

link E D4;

link G D4;

link Sonda D4;

link D4 Infección_2;

//Network Relationships: 

relation Sonda { 
comment = "new";
values= table (0.5 0.5 );
}

relation Infección Sonda { 
comment = "new";
values= table (0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 );
}

relation D Infección { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation E Infección { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation F Infección { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation G Infección { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation D_1 Infección Infección_2 { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation E_1 Infección Infección_2 { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation F_1 Infección Infección_2 { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation G_1 Infección Infección_2 { 
comment = "new";
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation Infección_2 Infección D4 { 
comment = "new";
values= table (0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 );
}

}
