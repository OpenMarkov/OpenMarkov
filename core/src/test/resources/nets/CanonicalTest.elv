// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =213;
pos_y =127;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =454;
pos_y =118;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =360;
pos_y =285;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =623;
pos_y =281;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =504;
pos_y =426;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =753;
pos_y =428;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node J(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =701;
pos_y =109;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A C;

link B C;

link B D;

link C E;

link D E;

link D F;

link J D;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.71 0.29 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.09 0.91 );
}

relation J { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.83 0.17 );
}

relation C A B { 
comment = "";
deterministic=false;
values= function  
          Or(CA,CB,CResidual);

henrionVSdiez = "Diez";
}

relation D B J { 
comment = "";
deterministic=false;
values= function  
          Min(DB,DJ,DResidual);

}

relation F D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.35 0.56 0.65 0.44 );
}

relation E C D { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(EC,ED,EResidual);

}

relation C A { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = CA;
deterministic=false;
values= table (0.52 0.0 0.48 1.0 );
}

relation C B { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = CB;
deterministic=false;
values= table (0.89 0.0 0.11 1.0 );
}

relation C { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = CResidual;
deterministic=false;
values= table (0.31 0.69 );
}

relation D B { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = DB;
deterministic=false;
values= table (0.23 0.43 0.77 0.57 );
}

relation D J { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = DJ;
deterministic=false;
values= table (0.12 0.49 0.88 0.51 );
}

relation D { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = DResidual;
deterministic=false;
values= table (0.6 0.4 );
}

relation E C { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = EC;
deterministic=false;
values= table (0.9 0.06 0.1 0.94 );
}

relation E D { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = ED;
deterministic=false;
values= table (0.23 0.35 0.77 0.65 );
}

relation E { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = EResidual;
deterministic=false;
values= table (0.32 0.68 );
}

}
