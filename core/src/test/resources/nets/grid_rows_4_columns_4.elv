//	   Bayesian Network
//	   Elvira format

bnet Grid_4_4 {

//		 Network Properties

default node states = (Presente, Ausente );

node Row0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =110;
//num-states = 2;
}

node Grid3_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =540;
//num-states = 2;
}

node Grid1_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =280;
//num-states = 2;
}

node Grid0_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =150;
//num-states = 2;
}

node Grid1_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =280;
//num-states = 2;
}

node Grid2_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =410;
//num-states = 2;
}

node Column2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =600;
pos_y =630;
//num-states = 2;
}

node Row3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =500;
//num-states = 2;
}

node Grid0_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =150;
//num-states = 2;
}

node Grid1_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =280;
//num-states = 2;
}

node Grid3_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =540;
//num-states = 2;
}

node Grid2_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =410;
//num-states = 2;
}

node Grid3_1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =320;
pos_y =540;
//num-states = 2;
}

node Row1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =240;
//num-states = 2;
}

node Grid0_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =150;
//num-states = 2;
}

node Grid2_3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =720;
pos_y =410;
//num-states = 2;
}

node Column3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =800;
pos_y =630;
//num-states = 2;
}

node Grid0_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =150;
//num-states = 2;
}

node Column0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =200;
pos_y =630;
//num-states = 2;
}

node Grid2_2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =520;
pos_y =410;
//num-states = 2;
}

node Grid3_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =540;
//num-states = 2;
}

node Column1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =400;
pos_y =630;
//num-states = 2;
}

node Row2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =370;
//num-states = 2;
}

node Grid1_0(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =280;
//num-states = 2;
}

//		 Links of the associated graph:

link Grid0_0 Row0;
link Grid0_0 Column0;
link Grid0_1 Row0;
link Grid0_1 Column1;
link Grid0_2 Row0;
link Grid0_2 Column2;
link Grid0_3 Row0;
link Grid0_3 Column3;
link Grid1_0 Row1;
link Grid1_0 Column0;
link Grid1_1 Row1;
link Grid1_1 Column1;
link Grid1_2 Row1;
link Grid1_2 Column2;
link Grid1_3 Row1;
link Grid1_3 Column3;
link Grid2_0 Row2;
link Grid2_0 Column0;
link Grid2_1 Row2;
link Grid2_1 Column1;
link Grid2_2 Row2;
link Grid2_2 Column2;
link Grid2_3 Row2;
link Grid2_3 Column3;
link Grid3_0 Row3;
link Grid3_0 Column0;
link Grid3_1 Row3;
link Grid3_1 Column1;
link Grid3_2 Row3;
link Grid3_2 Column2;
link Grid3_3 Row3;
link Grid3_3 Column3;

//		Network Relationships:

relation Grid0_0 {
values = table(0.06, 0.94);
}

relation Grid0_1 {
values = table(0.34, 0.66);
}

relation Grid0_2 {
values = table(0.8, 0.2);
}

relation Grid0_3 {
values = table(0.61, 0.39);
}

relation Grid1_0 {
values = table(0.5, 0.5);
}

relation Grid1_1 {
values = table(0.58, 0.42);
}

relation Grid1_2 {
values = table(0.62, 0.38);
}

relation Grid1_3 {
values = table(0.85, 0.15);
}

relation Grid2_0 {
values = table(0.47, 0.53);
}

relation Grid2_1 {
values = table(0.48, 0.52);
}

relation Grid2_2 {
values = table(0.64, 0.36);
}

relation Grid2_3 {
values = table(0.82, 0.18);
}

relation Grid3_0 {
values = table(0.35, 0.65);
}

relation Grid3_1 {
values = table(0.48, 0.52);
}

relation Grid3_2 {
values = table(0.66, 0.34);
}

relation Grid3_3 {
values = table(0.9, 0.1);
}

relation Row0 Grid0_3 Grid0_2 Grid0_1 Grid0_0 {
values = table(0.09, 0.16, 0.15, 0.95, 0.24, 0.66, 0.64, 0.6, 0.06, 0.6, 0.9, 0.88, 0.74, 0.97, 0.43, 0.35, 0.91, 0.84, 0.85, 0.05, 
0.76, 0.34, 0.36, 0.4, 0.94, 0.4, 0.1, 0.12, 0.26, 0.03, 0.57, 0.65);
}

relation Row1 Grid1_3 Grid1_2 Grid1_1 Grid1_0 {
values = table(0.12, 0.31, 0.26, 0.5, 0.59, 0.37, 0.16, 0.68, 0.75, 0.73, 0.53, 0.75, 0.15, 0.23, 0.34, 0.56, 0.88, 0.69, 0.74, 0.5, 
0.41, 0.63, 0.84, 0.32, 0.25, 0.27, 0.47, 0.25, 0.85, 0.77, 0.66, 0.44);
}

relation Row2 Grid2_3 Grid2_2 Grid2_1 Grid2_0 {
values = table(0.6, 0.43, 0.43, 0.29, 0.56, 0.54, 0.31, 0.02, 0.43, 0.66, 0.2, 0.54, 0.93, 0.25, 0.08, 0.9, 0.4, 0.57, 0.57, 0.71, 
0.44, 0.46, 0.69, 0.98, 0.57, 0.34, 0.8, 0.46, 0.07, 0.75, 0.92, 0.1);
}

relation Row3 Grid3_3 Grid3_2 Grid3_1 Grid3_0 {
values = table(0.08, 0.53, 0.91, 0.15, 0.61, 0.21, 0.1, 0.45, 0.24, 0.28, 0.73, 0.98, 0.79, 0.27, 0.5, 0.68, 0.92, 0.47, 0.09, 0.85, 
0.39, 0.79, 0.9, 0.55, 0.76, 0.72, 0.27, 0.02, 0.21, 0.73, 0.5, 0.32);
}

relation Column0 Grid3_0 Grid2_0 Grid1_0 Grid0_0 {
values = table(0.59, 0.72, 0.14, 0.08, 0.28, 0.52, 0.26, 0.69, 0.63, 0.04, 0.3, 0.43, 0.14, 0.21, 0.76, 0.05, 0.41, 0.28, 0.86, 0.92, 
0.72, 0.48, 0.74, 0.31, 0.37, 0.96, 0.7, 0.57, 0.86, 0.79, 0.24, 0.95);
}

relation Column1 Grid3_1 Grid2_1 Grid1_1 Grid0_1 {
values = table(0.61, 0.1, 0.95, 0.36, 0.24, 0.8, 0.02, 0.49, 0.31, 0.59, 0.25, 0.39, 0.81, 0.9, 0.22, 0.02, 0.39, 0.9, 0.05, 0.64, 
0.76, 0.2, 0.98, 0.51, 0.69, 0.41, 0.75, 0.61, 0.19, 0.1, 0.78, 0.98);
}

relation Column2 Grid3_2 Grid2_2 Grid1_2 Grid0_2 {
values = table(0.99, 0.78, 0.32, 0.79, 0.89, 0.03, 0.19, 0.96, 0.6, 0.58, 0.36, 0.62, 0.79, 0.95, 0.39, 0.32, 0.01, 0.22, 0.68, 0.21, 
0.11, 0.97, 0.81, 0.04, 0.4, 0.42, 0.64, 0.38, 0.21, 0.05, 0.61, 0.68);
}

relation Column3 Grid3_3 Grid2_3 Grid1_3 Grid0_3 {
values = table(0.64, 0.24, 0.83, 0.8, 0.33, 0.5, 0.12, 0.26, 0.93, 0.99, 0.41, 0.99, 0.9, 0.71, 0.87, 0.52, 0.36, 0.76, 0.17, 0.2, 
0.67, 0.5, 0.88, 0.74, 0.07, 0.01, 0.59, 0.01, 0.1, 0.29, 0.13, 0.48);
}

}

