// Bayesian Network
//   Elvira format 

bnet  boblo { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node f3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =413;
pos_y =356;
relevance = 7.0;
num-states = 2;
states = (present absent);
}

node f2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =467;
pos_y =351;
relevance = 7.0;
num-states = 2;
states = (present absent);
}

node asts2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =168;
pos_y =367;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node asts1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =243;
pos_y =349;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node astd2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =542;
pos_y =329;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node astd1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =415;
pos_y =218;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node sc(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =407;
pos_y =275;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

node dc(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =363;
pos_y =307;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

node pe(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =301;
pos_y =356;
relevance = 7.0;
num-states = 4;
states = (both_incorrect sire_incorrect dam_incorrect no_error);
}

node variable222(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =259;
pos_y =252;
relevance = 7.0;
num-states = 2;
states = (state0 state1);
}

node f1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =176;
pos_y =217;
relevance = 7.0;
num-states = 2;
states = (present absent);
}

node atd2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =434;
pos_y =110;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node atd1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =526;
pos_y =73;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node ats2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =354;
pos_y =202;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node ats1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =236;
pos_y =44;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node aph2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =179;
pos_y =118;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node aph1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =405;
pos_y =65;
relevance = 7.0;
num-states = 3;
states = (f1 f2 f3);
}

node ageno(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =501;
pos_y =214;
relevance = 7.0;
num-states = 6;
states = (f1_f1 f1_f2 f1_f3 f2_f2 f2_f3 f3_f3);
}

node f4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =612;
pos_y =295;
relevance = 7.0;
num-states = 2;
states = (present absent);
}

node variable20(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =74;
pos_y =209;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

node variable21(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =77;
pos_y =248;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

node variable22(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =106;
pos_y =296;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

node variable23(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =94;
pos_y =344;
relevance = 7.0;
num-states = 2;
states = (yes no);
}

// links of the associated graph:

link ageno f3;

link variable22 f3;

link ageno f2;

link variable21 f2;

link sc asts2;

link ats2 asts2;

link sc asts1;

link ats1 asts1;

link dc astd2;

link atd2 astd2;

link dc astd1;

link atd1 astd1;

link pe sc;

link pe dc;

link ageno f1;

link variable20 f1;

link ats1 aph2;

link ats2 aph2;

link atd1 aph1;

link atd2 aph1;

link aph1 ageno;

link aph2 ageno;

link ageno f4;

link variable23 f4;

//Network Relationships: 

relation f3 ageno variable22 { 
values= table (0.5 0.0 0.5 1.0 0.5 0.0 0.5 1.0 0.5 1.0 0.5 0.0 0.5 1.0 0.5 0.0 0.5 1.0 0.5 0.0 0.5 0.0 0.5 1.0 );
}

relation f2 ageno variable21 { 
values= table (0.5 1.0 0.5 1.0 0.5 1.0 0.5 0.0 0.5 1.0 0.5 1.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 1.0 0.5 0.0 0.5 0.0 );
}

relation asts2 sc ats2 { 
values= table (0.999 5.0E-4 5.0E-4 0.58 0.58 0.58 5.0E-4 0.999 5.0E-4 0.1 0.1 0.1 5.0E-4 5.0E-4 0.999 0.32 0.32 0.32 );
}

relation asts1 sc ats1 { 
values= table (0.999 5.0E-4 5.0E-4 0.58 0.58 0.58 5.0E-4 0.999 5.0E-4 0.1 0.1 0.1 5.0E-4 5.0E-4 0.999 0.32 0.32 0.32 );
}

relation astd2 dc atd2 { 
values= table (0.999 5.0E-4 5.0E-4 0.58 0.58 0.58 5.0E-4 0.999 5.0E-4 0.1 0.1 0.1 5.0E-4 5.0E-4 0.999 0.32 0.32 0.32 );
}

relation astd1 dc atd1 { 
values= table (0.999 5.0E-4 5.0E-4 0.58 0.58 0.58 5.0E-4 0.999 5.0E-4 0.1 0.1 0.1 5.0E-4 5.0E-4 0.999 0.32 0.32 0.32 );
}

relation sc pe { 
values= table (0.0 0.0 1.0 1.0 1.0 1.0 0.0 0.0 );
}

relation dc pe { 
values= table (0.0 1.0 0.0 1.0 1.0 0.0 1.0 0.0 );
}

relation pe { 
values= table (0.0045 0.0125 0.0018 0.9812 );
}

relation variable222 { 
values= table (0.5 0.5 );
}

relation f1 ageno variable20 { 
values= table (0.5 1.0 0.5 1.0 0.5 1.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 1.0 0.5 1.0 0.5 1.0 );
}

relation atd2 { 
values= table (0.58 0.1 0.32 );
}

relation atd1 { 
values= table (0.58 0.1 0.32 );
}

relation ats2 { 
values= table (0.58 0.1 0.32 );
}

relation ats1 { 
values= table (0.58 0.1 0.32 );
}

relation aph2 ats1 ats2 { 
values= table (1.0 0.5 0.5 0.5 0.0 0.0 0.5 0.0 0.0 0.0 0.5 0.0 0.5 1.0 0.5 0.0 0.5 0.0 0.0 0.0 0.5 0.0 0.0 0.5 0.5 0.5 1.0 );
}

relation aph1 atd1 atd2 { 
values= table (1.0 0.5 0.5 0.5 0.0 0.0 0.5 0.0 0.0 0.0 0.5 0.0 0.0 1.0 0.5 0.0 0.5 0.0 0.0 0.0 0.5 0.5 0.0 0.5 0.5 0.5 1.0 );
}

relation ageno aph1 aph2 { 
values= table (1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation f4 ageno variable23 { 
values= table (0.5 1.0 0.5 1.0 0.5 1.0 0.5 1.0 0.5 1.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 0.0 0.5 1.0 );
}

relation variable20 { 
values= table (0.0010 0.999 );
}

relation variable21 { 
values= table (0.0010 0.999 );
}

relation variable22 { 
values= table (0.0010 0.999 );
}

relation variable23 { 
values= table (0.0010 0.999 );
}

}
