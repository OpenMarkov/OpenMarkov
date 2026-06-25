// Bayesian Network
//   Elvira format 

bnet  "Unknown" { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node N0_7muVerMo(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongUp WeakUp Neutral Down);
}

node SubjVertMo(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StronUp WeakUp Neutral Down);
}

node QGVertMotion(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongUp WeakUp Neutral Down);
}

node CombVerMo(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongUp WeakUp Neutral Down);
}

node AreaMeso_ALS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongUp WeakUp Neutral Down);
}

node SatContMoist(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (VeryWet Wet Neutral Dry);
}

node RaoContMoist(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (VeryWet Wet Neutral Dry);
}

node CombMoisture(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (VeryWet Wet Neutral Dry);
}

node AreaMoDryAir(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (VeryWet Wet Neutral Dry);
}

node VISCloudCov(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Cloudy PC Clear);
}

node IRCloudCover(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Cloudy PC Clear);
}

node CombClouds(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Cloudy PC Clear);
}

node CldShadeOth(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Cloudy PC Clear);
}

node AMInstabMt(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Weak Strong);
}

node InsInMt(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Weak Strong);
}

node WndHodograph(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (DCVZFavor StrongWest Westerly Other);
}

node OutflowFrMt(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Weak Strong);
}

node MorningBound(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Weak Strong);
}

node Boundaries(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Weak Strong);
}

node CldShadeConv(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (None Some Marked);
}

node CompPlFcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (IncCapDecIns LittleChange DecCapIncIns);
}

node CapChange(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Decreasing LittleChange Increasing);
}

node LoLevMoistAd(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongPos WeakPos Neutral Negative);
}

node InsChange(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Decreasing LittleChange Increasing);
}

node MountainFcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (XNIL SIG SVR);
}

node Date(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 6;
states = (May15_Jun14 Jun15_Jul1 Jul2_Jul15 Jul16_Aug10 Aug11_Aug20 Aug20_Sep15);
}

node Scenario(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 11;
states = (A B C D E F G H I J K);
}

node ScenRelAMCIN(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (AB CThruK);
}

node MorningCIN(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (None PartInhibit Stifling TotalInhibit);
}

node AMCINInScen(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (LessThanAve Average MoreThanAve);
}

node CapInScen(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (LessThanAve Average MoreThanAve);
}

node ScenRelAMIns(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 6;
states = (ABI CDEJ F G H K);
}

node LIfr12ZDENSd(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (LIGt0 N1GtLIGt_4 N5GtLIGt_8 LILt_8);
}

node AMDewptCalPl(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (Instability Neutral Stability);
}

node AMInsWliScen(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (LessUnstable Average MoreUnstable);
}

node InsSclInScen(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (LessUnstable Average MoreUnstable);
}

node ScenRel3_4(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 5;
states = (ACEFK B D GJ HI);
}

node LatestCIN(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (None PartInhibit Stifling TotalInhibit);
}

node LLIW(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (Unfavorable Weak Moderate Strong);
}

node CurPropConv(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (None Slight Moderate Strong);
}

node ScnRelPlFcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 11;
states = (A B C D E F G H I J K);
}

node PlainsFcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (XNIL SIG SVR);
}

node N34StarFcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (XNIL SIG SVR);
}

node R5Fcst(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (XNIL SIG SVR);
}

node Dewpoints(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 7;
states = (LowEvrywhere LowAtStation LowSHighN LowNHighS LowMtsHighPl HighEvrywher Other);
}

node LowLLapse(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (CloseToDryAd Steep ModerateOrLe Stable);
}

node MeanRH(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (VeryMoist Average Dry);
}

node MidLLapse(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (CloseToDryAd Steep ModerateOrLe);
}

node MvmtFeatures(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (StrongFront MarkedUpper OtherRapid NoMajor);
}

node RHRatio(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 3;
states = (MoistMDryL DryMMoistL Other);
}

node SfcWndShfDis(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 7;
states = (DenvCyclone E_W_N E_W_S MovingFtorOt DryLine None Other);
}

node SynForcng(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 5;
states = (SigNegative NegToPos SigPositive PosToNeg LittleChange);
}

node TempDis(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (QStationary Moving None Other);
}

node WindAloft(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 4;
states = (LV SWQuad NWQuad AllElse);
}

node WindFieldMt(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 2;
states = (Westerly LVorOther);
}

node WindFieldPln(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
relevance = 7.0;
num-states = 6;
states = (LV DenvCyclone LongAnticyc E_NE SEQuad WidespdDnsl);
}

// links of the associated graph:

link Scenario MvmtFeatures;

link Scenario MidLLapse;

link AMInsWliScen InsSclInScen;

link InsChange InsSclInScen;

link AMCINInScen CapInScen;

link CapChange CapInScen;

link CompPlFcst CapChange;

link AreaMoDryAir CldShadeOth;

link AreaMeso_ALS CldShadeOth;

link CombClouds CldShadeOth;

link ScenRelAMCIN AMCINInScen;

link MorningCIN AMCINInScen;

link InsInMt CldShadeConv;

link WndHodograph CldShadeConv;

link AreaMeso_ALS AreaMoDryAir;

link CombMoisture AreaMoDryAir;

link Scenario Dewpoints;

link CurPropConv PlainsFcst;

link InsSclInScen PlainsFcst;

link CapInScen PlainsFcst;

link ScnRelPlFcst PlainsFcst;

link LoLevMoistAd InsChange;

link CompPlFcst InsChange;

link Scenario SfcWndShfDis;

link Boundaries CompPlFcst;

link CldShadeConv CompPlFcst;

link AreaMeso_ALS CompPlFcst;

link CldShadeOth CompPlFcst;

link Scenario RHRatio;

link Scenario ScenRelAMIns;

link Scenario ScenRelAMCIN;

link OutflowFrMt Boundaries;

link WndHodograph Boundaries;

link MorningBound Boundaries;

link CldShadeOth InsInMt;

link AMInstabMt InsInMt;

link CombVerMo AreaMeso_ALS;

link Scenario WindFieldPln;

link LatestCIN CurPropConv;

link LLIW CurPropConv;

link Scenario TempDis;

link Scenario SynForcng;

link Scenario MeanRH;

link Scenario LowLLapse;

link SatContMoist CombMoisture;

link RaoContMoist CombMoisture;

link Date Scenario;

link ScenRel3_4 N34StarFcst;

link PlainsFcst N34StarFcst;

link Scenario WindFieldMt;

link Scenario ScnRelPlFcst;

link ScenRelAMIns AMInsWliScen;

link LIfr12ZDENSd AMInsWliScen;

link AMDewptCalPl AMInsWliScen;

link InsInMt MountainFcst;

link InsInMt OutflowFrMt;

link WndHodograph OutflowFrMt;

link Scenario WindAloft;

link MountainFcst R5Fcst;

link N34StarFcst R5Fcst;

link Scenario ScenRel3_4;

link VISCloudCov CombClouds;

link IRCloudCover CombClouds;

link N0_7muVerMo CombVerMo;

link SubjVertMo CombVerMo;

link QGVertMotion CombVerMo;

//Network Relationships: 

relation MvmtFeatures Scenario { 
values= table (0.25 0.05 0.1 0.18 0.02 0.05 0.1 0.0 0.2 0.04 0.5 0.55 0.1 0.3 0.38 0.02 0.07 0.25 0.6 0.1 0.0 0.35 0.2 0.1 0.3 0.34 0.26 0.05 0.15 0.1 0.2 0.04 0.09 0.0 0.75 0.3 0.1 0.7 0.83 0.5 0.3 0.5 0.92 0.06 );
}

relation AMDewptCalPl { 
values= table (0.3 0.25 0.45 );
}

relation MorningBound { 
values= table (0.5 0.35 0.15 );
}

relation MidLLapse Scenario { 
values= table (0.25 0.25 0.4 0.43 0.02 0.0 0.84 0.25 0.41 0.23 0.16 0.55 0.5 0.38 0.37 0.38 0.1 0.16 0.31 0.29 0.42 0.28 0.2 0.25 0.22 0.2 0.6 0.9 0.0 0.44 0.3 0.35 0.56 );
}

relation InsSclInScen AMInsWliScen InsChange { 
values= table (1.0 0.9 0.4 0.6 0.15 0.0 0.25 0.0 0.0 0.0 0.1 0.35 0.4 0.7 0.4 0.35 0.1 0.0 0.0 0.0 0.25 0.0 0.15 0.6 0.4 0.9 1.0 );
}

relation CapInScen AMCINInScen CapChange { 
values= table (1.0 0.98 0.35 0.75 0.03 0.0 0.3 0.0 0.0 0.0 0.02 0.35 0.25 0.94 0.25 0.35 0.02 0.0 0.0 0.0 0.3 0.0 0.03 0.75 0.35 0.98 1.0 );
}

relation LoLevMoistAd { 
values= table (0.12 0.28 0.3 0.3 );
}

relation CapChange CompPlFcst { 
values= table (0.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0 0.0 );
}

relation CldShadeOth AreaMoDryAir AreaMeso_ALS CombClouds { 
values= table (1.0 0.85 0.25 0.95 0.4 0.05 0.93 0.2 0.01 0.74 0.0 0.0 0.92 0.7 0.15 0.9 0.25 0.01 0.8 0.01 0.0 0.65 0.0 0.0 0.88 0.4 0.1 0.85 0.15 0.0 0.8 0.03 0.0 0.5 0.01 0.0 0.85 0.55 0.1 0.6 0.01 0.0 0.78 0.01 0.0 0.42 0.05 0.0 0.0 0.15 0.35 0.05 0.55 0.45 0.07 0.78 0.29 0.25 0.5 0.1 0.08 0.29 0.4 0.09 0.6 0.3 0.2 0.89 0.1 0.34 0.4 0.02 0.12 0.5 0.4 0.15 0.75 0.2 0.18 0.85 0.05 0.48 0.74 0.01 0.14 0.43 0.25 0.39 0.9 0.15 0.2 0.74 0.04 0.55 0.65 0.0 0.0 0.0 0.4 0.0 0.05 0.5 0.0 0.02 0.7 0.01 0.5 0.9 0.0 0.01 0.45 0.01 0.15 0.69 0.0 0.1 0.9 0.01 0.6 0.98 0.0 0.1 0.5 0.0 0.1 0.8 0.02 0.12 0.95 0.02 0.25 0.99 0.01 0.02 0.65 0.01 0.09 0.85 0.02 0.25 0.96 0.03 0.3 1.0 );
}

relation QGVertMotion { 
values= table (0.15 0.15 0.5 0.2 );
}

relation AMCINInScen ScenRelAMCIN MorningCIN { 
values= table (1.0 0.6 0.25 0.0 0.75 0.3 0.01 0.0 0.0 0.37 0.45 0.1 0.25 0.6 0.4 0.03 0.0 0.03 0.3 0.9 0.0 0.1 0.59 0.97 );
}

relation MorningCIN { 
values= table (0.15 0.57 0.2 0.08 );
}

relation Date { 
values= table (0.254098 0.131148 0.106557 0.213115 0.07377 0.221312 );
}

relation CldShadeConv InsInMt WndHodograph { 
values= table (1.0 1.0 1.0 1.0 0.3 0.2 0.5 0.8 0.0 0.0 0.1 0.5 0.0 0.0 0.0 0.0 0.6 0.7 0.46 0.19 0.3 0.2 0.5 0.38 0.0 0.0 0.0 0.0 0.1 0.1 0.04 0.01 0.7 0.8 0.4 0.12 );
}

relation AMInstabMt { 
values= table (0.333333 0.333333 0.333334 );
}

relation AreaMoDryAir AreaMeso_ALS CombMoisture { 
values= table (0.99 0.7 0.2 0.0 0.8 0.35 0.01 0.0 0.7 0.2 0.01 0.0 0.2 0.05 0.0 0.0 0.01 0.29 0.55 0.25 0.2 0.55 0.39 0.02 0.29 0.6 0.09 0.0 0.74 0.4 0.05 0.0 0.0 0.01 0.24 0.55 0.0 0.1 0.55 0.43 0.01 0.2 0.8 0.3 0.06 0.45 0.5 0.01 0.0 0.0 0.01 0.2 0.0 0.0 0.05 0.55 0.0 0.0 0.1 0.7 0.0 0.1 0.45 0.99 );
}

relation Dewpoints Scenario { 
values= table (0.04 0.05 0.4 0.13 0.15 0.0 0.5 0.0 0.0 0.1 0.1 0.05 0.07 0.25 0.22 0.2 0.0 0.27 0.02 0.02 0.45 0.1 0.15 0.15 0.0 0.18 0.2 0.0 0.15 0.1 0.7 0.1 0.1 0.05 0.1 0.15 0.07 0.18 0.0 0.02 0.05 0.0 0.05 0.2 0.19 0.3 0.05 0.34 0.11 0.0 0.02 0.5 0.2 0.26 0.05 0.3 0.27 0.02 0.03 0.11 0.98 0.0 0.2 0.04 0.02 0.1 0.22 0.06 0.13 0.03 0.05 0.02 0.04 0.13 0.04 0.02 0.35 );
}

relation PlainsFcst CurPropConv InsSclInScen CapInScen ScnRelPlFcst { 
values= table (0.75 0.75 0.9 0.9 0.88 0.92 0.85 1.0 0.9 0.9 0.95 0.75 0.65 0.9 0.91 0.85 0.9 0.84 0.99 0.88 0.92 0.96 0.75 0.75 0.95 0.93 0.92 0.87 0.9 0.98 0.92 0.95 0.97 0.5 0.6 0.8 0.85 0.85 0.88 0.8 0.92 0.8 0.75 0.9 0.35 0.55 0.82 0.82 0.75 0.88 0.75 0.9 0.7 0.8 0.9 0.5 0.6 0.85 0.85 0.75 0.85 0.75 0.94 0.65 0.83 0.93 0.35 0.45 0.8 0.72 0.78 0.86 0.65 0.85 0.65 0.72 0.85 0.25 0.45 0.65 0.55 0.55 0.81 0.6 0.8 0.6 0.75 0.88 0.4 0.45 0.75 0.65 0.52 0.82 0.65 0.85 0.5 0.77 0.9 0.7 0.6 0.82 0.85 0.82 0.85 0.8 0.97 0.88 0.86 0.88 0.65 0.58 0.8 0.85 0.8 0.83 0.77 0.93 0.85 0.85 0.9 0.6 0.65 0.9 0.85 0.82 0.8 0.8 0.91 0.85 0.9 0.93 0.3 0.55 0.7 0.75 0.62 0.85 0.75 0.82 0.6 0.68 0.82 0.28 0.48 0.7 0.7 0.6 0.82 0.63 0.8 0.5 0.7 0.8 0.4 0.5 0.72 0.65 0.55 0.78 0.55 0.85 0.45 0.73 0.85 0.3 0.4 0.65 0.6 0.6 0.83 0.45 0.7 0.55 0.6 0.72 0.22 0.35 0.45 0.45 0.48 0.72 0.43 0.68 0.35 0.6 0.74 0.27 0.35 0.55 0.45 0.42 0.74 0.45 0.77 0.3 0.68 0.75 0.5 0.45 0.75 0.75 0.72 0.78 0.66 0.88 0.7 0.78 0.8 0.45 0.45 0.7 0.72 0.7 0.75 0.62 0.85 0.75 0.76 0.8 0.35 0.45 0.75 0.7 0.6 0.72 0.6 0.8 0.75 0.75 0.88 0.2 0.4 0.7 0.65 0.5 0.74 0.6 0.67 0.35 0.6 0.75 0.23 0.38 0.58 0.55 0.53 0.73 0.35 0.65 0.3 0.6 0.68 0.3 0.35 0.55 0.5 0.4 0.7 0.35 0.6 0.35 0.62 0.7 0.25 0.3 0.45 0.5 0.4 0.72 0.25 0.57 0.25 0.48 0.6 0.19 0.25 0.35 0.35 0.35 0.65 0.22 0.45 0.25 0.48 0.58 0.15 0.25 0.4 0.3 0.25 0.6 0.18 0.47 0.25 0.5 0.5 0.4 0.35 0.6 0.6 0.55 0.69 0.54 0.75 0.55 0.7 0.7 0.35 0.35 0.55 0.55 0.5 0.65 0.38 0.7 0.65 0.67 0.7 0.2 0.3 0.55 0.5 0.45 0.6 0.28 0.65 0.63 0.62 0.8 0.16 0.3 0.45 0.52 0.35 0.65 0.48 0.58 0.25 0.5 0.65 0.18 0.3 0.45 0.45 0.35 0.62 0.2 0.52 0.23 0.47 0.55 0.23 0.25 0.4 0.4 0.3 0.57 0.15 0.5 0.25 0.5 0.55 0.18 0.2 0.3 0.4 0.25 0.63 0.15 0.4 0.2 0.3 0.5 0.15 0.18 0.25 0.25 0.25 0.58 0.13 0.3 0.22 0.35 0.5 0.1 0.2 0.2 0.23 0.15 0.5 0.1 0.28 0.2 0.3 0.38 0.2 0.2 0.08 0.06 0.1 0.08 0.13 0.0 0.08 0.08 0.04 0.2 0.3 0.08 0.05 0.13 0.1 0.12 0.01 0.1 0.06 0.03 0.2 0.2 0.04 0.04 0.06 0.13 0.06 0.02 0.06 0.04 0.02 0.3 0.3 0.14 0.09 0.1 0.11 0.17 0.06 0.12 0.22 0.08 0.3 0.3 0.13 0.1 0.18 0.11 0.2 0.07 0.2 0.15 0.08 0.2 0.25 0.1 0.07 0.15 0.14 0.2 0.05 0.22 0.1 0.06 0.2 0.35 0.1 0.14 0.15 0.12 0.25 0.1 0.2 0.2 0.1 0.15 0.35 0.2 0.2 0.25 0.17 0.28 0.13 0.2 0.15 0.08 0.08 0.25 0.1 0.15 0.25 0.16 0.27 0.09 0.2 0.1 0.07 0.25 0.33 0.13 0.1 0.15 0.14 0.17 0.02 0.1 0.1 0.1 0.25 0.32 0.15 0.1 0.16 0.16 0.17 0.06 0.12 0.1 0.08 0.3 0.28 0.08 0.1 0.13 0.19 0.13 0.08 0.12 0.08 0.06 0.4 0.34 0.2 0.15 0.28 0.14 0.2 0.14 0.25 0.22 0.15 0.37 0.35 0.2 0.17 0.29 0.16 0.3 0.15 0.3 0.2 0.16 0.28 0.25 0.18 0.2 0.3 0.2 0.35 0.12 0.3 0.15 0.12 0.25 0.36 0.2 0.2 0.28 0.14 0.4 0.18 0.25 0.25 0.2 0.17 0.37 0.3 0.25 0.29 0.25 0.4 0.2 0.3 0.2 0.16 0.1 0.3 0.22 0.25 0.3 0.22 0.4 0.13 0.25 0.15 0.15 0.4 0.42 0.18 0.15 0.22 0.21 0.27 0.1 0.22 0.16 0.16 0.35 0.35 0.2 0.17 0.22 0.24 0.3 0.12 0.15 0.17 0.16 0.4 0.4 0.19 0.2 0.3 0.27 0.3 0.16 0.17 0.2 0.1 0.45 0.4 0.2 0.22 0.34 0.24 0.3 0.24 0.4 0.25 0.2 0.4 0.35 0.25 0.25 0.32 0.25 0.53 0.24 0.4 0.24 0.24 0.34 0.35 0.25 0.27 0.38 0.28 0.5 0.25 0.35 0.22 0.22 0.28 0.38 0.3 0.25 0.35 0.24 0.57 0.28 0.35 0.26 0.26 0.18 0.4 0.3 0.3 0.35 0.3 0.58 0.35 0.34 0.26 0.25 0.16 0.3 0.3 0.3 0.4 0.34 0.62 0.3 0.3 0.22 0.27 0.45 0.45 0.27 0.22 0.32 0.29 0.36 0.2 0.3 0.22 0.25 0.4 0.4 0.3 0.27 0.35 0.33 0.5 0.24 0.2 0.23 0.25 0.45 0.45 0.3 0.3 0.38 0.38 0.57 0.28 0.25 0.28 0.17 0.47 0.45 0.32 0.26 0.45 0.32 0.39 0.3 0.45 0.28 0.27 0.45 0.35 0.3 0.3 0.43 0.35 0.65 0.33 0.42 0.3 0.3 0.4 0.4 0.3 0.3 0.45 0.4 0.65 0.33 0.36 0.28 0.3 0.3 0.4 0.3 0.3 0.48 0.32 0.63 0.38 0.37 0.35 0.32 0.2 0.4 0.35 0.35 0.42 0.36 0.62 0.45 0.35 0.32 0.3 0.2 0.3 0.4 0.3 0.45 0.42 0.65 0.4 0.32 0.28 0.32 0.05 0.05 0.02 0.04 0.02 0.0 0.02 0.0 0.02 0.02 0.01 0.05 0.05 0.02 0.04 0.02 0.0 0.04 0.0 0.02 0.02 0.01 0.05 0.05 0.01 0.03 0.02 0.0 0.04 0.0 0.02 0.01 0.01 0.2 0.1 0.06 0.06 0.05 0.01 0.03 0.02 0.08 0.03 0.02 0.35 0.15 0.05 0.08 0.07 0.01 0.05 0.03 0.1 0.05 0.02 0.3 0.15 0.05 0.08 0.1 0.01 0.05 0.01 0.13 0.07 0.01 0.45 0.2 0.1 0.14 0.07 0.02 0.1 0.05 0.15 0.08 0.05 0.6 0.2 0.15 0.25 0.2 0.02 0.12 0.07 0.2 0.1 0.04 0.52 0.3 0.15 0.2 0.23 0.02 0.08 0.06 0.3 0.13 0.03 0.05 0.07 0.05 0.05 0.03 0.01 0.03 0.01 0.02 0.04 0.02 0.1 0.1 0.05 0.05 0.04 0.01 0.06 0.01 0.03 0.05 0.02 0.1 0.07 0.02 0.05 0.05 0.01 0.07 0.01 0.03 0.02 0.01 0.3 0.11 0.1 0.1 0.1 0.01 0.05 0.04 0.15 0.1 0.03 0.35 0.17 0.1 0.13 0.11 0.02 0.07 0.05 0.2 0.1 0.04 0.32 0.25 0.1 0.15 0.15 0.02 0.1 0.03 0.25 0.12 0.03 0.45 0.24 0.15 0.2 0.12 0.03 0.15 0.12 0.2 0.15 0.08 0.61 0.28 0.25 0.3 0.23 0.03 0.17 0.12 0.35 0.2 0.1 0.63 0.35 0.23 0.3 0.28 0.04 0.15 0.1 0.45 0.17 0.1 0.1 0.13 0.07 0.1 0.06 0.01 0.07 0.02 0.08 0.06 0.04 0.2 0.2 0.1 0.11 0.08 0.01 0.08 0.03 0.1 0.07 0.04 0.25 0.15 0.06 0.1 0.1 0.01 0.1 0.04 0.08 0.05 0.02 0.35 0.2 0.1 0.13 0.16 0.02 0.1 0.09 0.25 0.15 0.05 0.37 0.27 0.17 0.2 0.15 0.02 0.12 0.11 0.3 0.16 0.08 0.36 0.3 0.2 0.23 0.22 0.02 0.15 0.15 0.3 0.16 0.08 0.47 0.32 0.25 0.25 0.25 0.04 0.18 0.15 0.4 0.26 0.14 0.63 0.35 0.35 0.35 0.3 0.05 0.2 0.2 0.41 0.26 0.17 0.69 0.45 0.3 0.4 0.35 0.06 0.2 0.23 0.45 0.28 0.23 0.15 0.2 0.13 0.18 0.13 0.02 0.1 0.05 0.15 0.08 0.05 0.25 0.25 0.15 0.18 0.15 0.02 0.12 0.06 0.15 0.1 0.05 0.35 0.25 0.15 0.2 0.17 0.02 0.15 0.07 0.12 0.1 0.03 0.37 0.25 0.23 0.22 0.2 0.03 0.13 0.12 0.3 0.22 0.08 0.37 0.35 0.25 0.25 0.22 0.03 0.15 0.15 0.35 0.23 0.15 0.37 0.35 0.3 0.3 0.25 0.03 0.2 0.17 0.39 0.22 0.15 0.52 0.4 0.4 0.3 0.27 0.05 0.22 0.22 0.43 0.35 0.18 0.65 0.42 0.4 0.4 0.33 0.06 0.25 0.25 0.43 0.33 0.2 0.7 0.5 0.4 0.47 0.4 0.08 0.25 0.32 0.48 0.42 0.3 );
}

relation LLIW { 
values= table (0.12 0.32 0.38 0.18 );
}

relation InsChange LoLevMoistAd CompPlFcst { 
values= table (0.0 0.0 0.05 0.05 0.1 0.25 0.15 0.2 0.35 0.5 0.8 0.9 0.05 0.12 0.15 0.15 0.4 0.5 0.5 0.6 0.5 0.4 0.16 0.09 0.95 0.88 0.8 0.8 0.5 0.25 0.35 0.2 0.15 0.1 0.04 0.01 );
}

relation IRCloudCover { 
values= table (0.15 0.45 0.4 );
}

relation SfcWndShfDis Scenario { 
values= table (0.65 0.65 0.0 0.12 0.06 0.1 0.02 0.01 0.02 0.06 0.05 0.05 0.05 0.65 0.02 0.14 0.1 0.05 0.1 0.1 0.08 0.13 0.1 0.1 0.2 0.02 0.04 0.1 0.05 0.15 0.5 0.04 0.05 0.08 0.1 0.02 0.02 0.04 0.02 0.0 0.4 0.3 0.02 0.39 0.04 0.02 0.06 0.45 0.25 0.0 0.35 0.0 0.01 0.6 0.13 0.07 0.07 0.05 0.27 0.4 0.56 0.33 0.23 0.02 0.14 0.15 0.01 0.01 0.02 0.1 0.07 0.12 0.2 0.11 0.05 0.06 0.1 );
}

relation LatestCIN { 
values= table (0.4 0.4 0.15 0.05 );
}

relation CompPlFcst Boundaries CldShadeConv AreaMeso_ALS CldShadeOth { 
values= table (0.4 0.1 0.05 0.6 0.4 0.2 0.6 0.45 0.25 0.7 0.65 0.6 0.4 0.25 0.15 0.65 0.45 0.25 0.65 0.5 0.3 0.75 0.7 0.65 0.45 0.4 0.35 0.7 0.55 0.4 0.7 0.6 0.55 0.85 0.8 0.75 0.35 0.05 0.03 0.5 0.3 0.15 0.55 0.4 0.2 0.6 0.6 0.55 0.35 0.1 0.05 0.55 0.35 0.2 0.6 0.45 0.25 0.65 0.65 0.6 0.4 0.25 0.2 0.65 0.45 0.3 0.65 0.55 0.5 0.78 0.75 0.7 0.3 0.01 0.01 0.35 0.15 0.1 0.45 0.3 0.15 0.5 0.48 0.45 0.3 0.05 0.04 0.4 0.2 0.12 0.5 0.35 0.2 0.55 0.55 0.5 0.3 0.15 0.13 0.5 0.35 0.2 0.55 0.45 0.4 0.7 0.65 0.6 0.35 0.35 0.3 0.25 0.3 0.5 0.35 0.4 0.45 0.27 0.3 0.35 0.35 0.3 0.35 0.25 0.3 0.5 0.3 0.4 0.45 0.23 0.26 0.32 0.3 0.3 0.3 0.22 0.3 0.45 0.27 0.3 0.33 0.14 0.17 0.23 0.35 0.35 0.25 0.25 0.35 0.45 0.3 0.4 0.4 0.35 0.3 0.33 0.35 0.35 0.3 0.25 0.35 0.5 0.3 0.4 0.5 0.3 0.3 0.35 0.35 0.4 0.4 0.25 0.35 0.5 0.3 0.3 0.3 0.18 0.2 0.25 0.3 0.25 0.2 0.25 0.4 0.35 0.3 0.4 0.4 0.35 0.32 0.35 0.3 0.6 0.27 0.25 0.4 0.43 0.3 0.4 0.45 0.35 0.3 0.4 0.35 0.35 0.35 0.25 0.35 0.45 0.35 0.35 0.35 0.24 0.28 0.3 0.25 0.55 0.65 0.15 0.3 0.3 0.05 0.15 0.3 0.03 0.05 0.05 0.25 0.45 0.5 0.1 0.25 0.25 0.05 0.1 0.25 0.02 0.04 0.03 0.25 0.3 0.35 0.08 0.15 0.15 0.03 0.1 0.12 0.01 0.03 0.02 0.3 0.6 0.72 0.25 0.35 0.4 0.15 0.2 0.4 0.05 0.1 0.12 0.3 0.55 0.65 0.2 0.3 0.3 0.1 0.15 0.25 0.05 0.05 0.05 0.25 0.35 0.4 0.1 0.2 0.2 0.05 0.15 0.2 0.04 0.05 0.05 0.4 0.74 0.79 0.4 0.45 0.55 0.25 0.3 0.45 0.15 0.2 0.2 0.4 0.35 0.69 0.35 0.4 0.45 0.2 0.25 0.35 0.1 0.15 0.1 0.35 0.5 0.52 0.25 0.3 0.35 0.1 0.2 0.25 0.06 0.07 0.1 );
}

relation N0_7muVerMo { 
values= table (0.25 0.25 0.25 0.25 );
}

relation RHRatio Scenario { 
values= table (0.05 0.1 0.4 0.2 0.8 0.0 0.6 0.0 0.1 0.4 0.15 0.5 0.5 0.15 0.45 0.05 0.0 0.0 0.7 0.7 0.4 0.45 0.45 0.4 0.45 0.35 0.15 1.0 0.4 0.3 0.2 0.2 0.4 );
}

relation ScenRelAMIns Scenario { 
values= table (1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation ScenRelAMCIN Scenario { 
values= table (1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 );
}

relation Boundaries OutflowFrMt WndHodograph MorningBound { 
values= table (0.5 0.3 0.1 0.75 0.45 0.25 0.8 0.35 0.25 0.7 0.25 0.05 0.3 0.1 0.05 0.15 0.1 0.05 0.15 0.05 0.05 0.4 0.2 0.05 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.02 0.01 0.01 0.48 0.5 0.25 0.22 0.45 0.4 0.18 0.5 0.35 0.28 0.6 0.35 0.63 0.5 0.2 0.7 0.75 0.5 0.7 0.8 0.45 0.55 0.65 0.3 0.55 0.4 0.15 0.5 0.4 0.2 0.7 0.5 0.2 0.73 0.5 0.2 0.02 0.2 0.65 0.03 0.1 0.35 0.02 0.15 0.4 0.02 0.15 0.6 0.07 0.4 0.75 0.15 0.15 0.45 0.15 0.15 0.5 0.05 0.15 0.65 0.45 0.6 0.85 0.5 0.6 0.8 0.3 0.5 0.8 0.25 0.49 0.79 );
}

relation InsInMt CldShadeOth AMInstabMt { 
values= table (0.9 0.01 0.0 0.6 0.0 0.0 0.5 0.0 0.0 0.1 0.4 0.05 0.39 0.4 0.0 0.35 0.15 0.0 0.0 0.59 0.95 0.01 0.6 1.0 0.15 0.85 1.0 );
}

relation RaoContMoist { 
values= table (0.15 0.2 0.4 0.25 );
}

relation AreaMeso_ALS CombVerMo { 
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation SubjVertMo { 
values= table (0.15 0.15 0.5 0.2 );
}

relation WindFieldPln Scenario { 
values= table (0.05 0.08 0.1 0.1 0.43 0.6 0.25 0.04 0.2 0.6 0.1 0.6 0.6 0.0 0.15 0.1 0.07 0.01 0.02 0.3 0.08 0.05 0.02 0.02 0.75 0.2 0.15 0.01 0.3 0.04 0.05 0.07 0.1 0.1 0.1 0.0 0.05 0.06 0.12 0.01 0.8 0.37 0.03 0.05 0.23 0.2 0.0 0.3 0.06 0.2 0.03 0.1 0.07 0.2 0.2 0.0 0.0 0.15 0.2 0.2 0.0 0.4 0.0 0.01 0.02 0.5 );
}

relation CurPropConv LatestCIN LLIW { 
values= table (0.7 0.1 0.01 0.0 0.9 0.65 0.25 0.01 0.95 0.75 0.4 0.2 1.0 0.95 0.75 0.5 0.28 0.5 0.14 0.02 0.09 0.25 0.35 0.15 0.05 0.23 0.4 0.3 0.0 0.05 0.2 0.35 0.02 0.3 0.35 0.18 0.01 0.09 0.3 0.33 0.0 0.02 0.18 0.35 0.0 0.0 0.05 0.1 0.0 0.1 0.5 0.8 0.0 0.01 0.1 0.51 0.0 0.0 0.02 0.15 0.0 0.0 0.0 0.05 );
}

relation TempDis Scenario { 
values= table (0.13 0.15 0.12 0.1 0.04 0.05 0.03 0.05 0.8 0.1 0.2 0.15 0.15 0.1 0.15 0.04 0.12 0.03 0.4 0.19 0.05 0.3 0.1 0.25 0.35 0.4 0.82 0.75 0.84 0.5 0.0 0.4 0.3 0.62 0.45 0.43 0.35 0.1 0.08 0.1 0.05 0.01 0.45 0.2 );
}

relation SynForcng Scenario { 
values= table (0.35 0.06 0.1 0.35 0.15 0.15 0.15 0.25 0.25 0.01 0.2 0.25 0.1 0.27 0.2 0.15 0.1 0.1 0.25 0.2 0.05 0.2 0.0 0.06 0.4 0.1 0.1 0.05 0.1 0.25 0.15 0.01 0.35 0.35 0.3 0.08 0.25 0.15 0.15 0.25 0.15 0.2 0.05 0.15 0.05 0.48 0.15 0.1 0.45 0.55 0.4 0.1 0.2 0.88 0.1 );
}

relation MeanRH Scenario { 
values= table (0.33 0.4 0.05 0.1 0.05 1.0 0.0 0.4 0.2 0.05 0.2 0.5 0.4 0.45 0.5 0.65 0.0 0.07 0.55 0.45 0.55 0.4 0.17 0.2 0.5 0.4 0.3 0.0 0.93 0.05 0.35 0.4 0.4 );
}

relation LowLLapse Scenario { 
values= table (0.04 0.07 0.35 0.4 0.45 0.01 0.78 0.0 0.22 0.13 0.09 0.25 0.31 0.47 0.4 0.35 0.35 0.19 0.02 0.4 0.4 0.4 0.35 0.31 0.14 0.13 0.15 0.45 0.03 0.33 0.3 0.35 0.33 0.36 0.31 0.04 0.07 0.05 0.19 0.0 0.65 0.08 0.12 0.18 );
}

relation WndHodograph { 
values= table (0.3 0.25 0.25 0.2 );
}

relation CombMoisture SatContMoist RaoContMoist { 
values= table (0.9 0.6 0.3 0.25 0.55 0.15 0.05 0.1 0.25 0.1 0.0 0.0 0.25 0.25 0.25 0.25 0.1 0.35 0.5 0.35 0.4 0.6 0.4 0.3 0.3 0.35 0.15 0.1 0.25 0.25 0.25 0.25 0.0 0.05 0.2 0.25 0.05 0.2 0.45 0.3 0.35 0.5 0.7 0.4 0.25 0.25 0.25 0.25 0.0 0.0 0.0 0.15 0.0 0.05 0.1 0.3 0.1 0.05 0.15 0.5 0.25 0.25 0.25 0.25 );
}

relation LIfr12ZDENSd { 
values= table (0.1 0.52 0.3 0.08 );
}

relation Scenario Date { 
values= table (0.1 0.05 0.04 0.04 0.04 0.05 0.16 0.16 0.13 0.13 0.11 0.11 0.1 0.09 0.1 0.09 0.1 0.1 0.08 0.09 0.08 0.07 0.07 0.08 0.08 0.12 0.15 0.2 0.17 0.11 0.01 0.02 0.03 0.08 0.05 0.02 0.08 0.13 0.14 0.06 0.1 0.11 0.1 0.06 0.04 0.05 0.05 0.06 0.09 0.07 0.06 0.07 0.07 0.08 0.03 0.11 0.15 0.13 0.14 0.11 0.17 0.1 0.08 0.08 0.1 0.17 );
}

relation N34StarFcst ScenRel3_4 PlainsFcst { 
values= table (0.94 0.06 0.01 0.98 0.04 0.0 0.92 0.01 0.0 0.92 0.03 0.01 0.99 0.09 0.03 0.05 0.89 0.05 0.02 0.94 0.03 0.06 0.89 0.01 0.06 0.92 0.04 0.01 0.9 0.12 0.01 0.05 0.94 0.0 0.02 0.97 0.02 0.1 0.99 0.02 0.05 0.95 0.0 0.01 0.85 );
}

relation SatContMoist { 
values= table (0.15 0.2 0.4 0.25 );
}

relation WindFieldMt Scenario { 
values= table (0.8 0.35 0.75 0.7 0.65 0.15 0.7 0.3 0.5 0.01 0.7 0.2 0.65 0.25 0.3 0.35 0.85 0.3 0.7 0.5 0.99 0.3 );
}

relation ScnRelPlFcst Scenario { 
values= table (1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation AMInsWliScen ScenRelAMIns LIfr12ZDENSd AMDewptCalPl { 
values= table (0.6 0.85 0.95 0.3 0.5 0.75 0.06 0.2 0.5 0.01 0.05 0.35 0.4 0.7 0.9 0.15 0.25 0.6 0.03 0.2 0.45 0.01 0.05 0.25 0.35 0.55 0.85 0.07 0.2 0.5 0.0 0.05 0.25 0.0 0.0 0.04 0.3 0.5 0.75 0.15 0.2 0.15 0.07 0.13 0.1 0.02 0.04 0.07 0.35 0.4 0.58 0.1 0.15 0.4 0.02 0.05 0.15 0.01 0.03 0.08 0.3 0.4 0.5 0.1 0.25 0.3 0.05 0.1 0.15 0.02 0.04 0.1 0.3 0.13 0.04 0.3 0.3 0.2 0.21 0.4 0.4 0.04 0.2 0.35 0.3 0.2 0.08 0.3 0.5 0.3 0.17 0.3 0.4 0.04 0.18 0.4 0.35 0.4 0.13 0.38 0.6 0.43 0.05 0.35 0.5 0.02 0.05 0.16 0.4 0.3 0.2 0.35 0.6 0.7 0.23 0.47 0.75 0.18 0.26 0.3 0.45 0.5 0.4 0.25 0.45 0.45 0.18 0.25 0.35 0.09 0.17 0.32 0.55 0.5 0.43 0.35 0.5 0.5 0.22 0.35 0.35 0.1 0.16 0.25 0.1 0.02 0.01 0.4 0.2 0.05 0.73 0.4 0.1 0.95 0.75 0.3 0.3 0.1 0.02 0.55 0.25 0.1 0.8 0.5 0.15 0.95 0.77 0.35 0.3 0.05 0.02 0.55 0.2 0.07 0.95 0.6 0.25 0.98 0.95 0.8 0.3 0.2 0.05 0.5 0.2 0.15 0.7 0.4 0.15 0.8 0.7 0.63 0.2 0.1 0.02 0.65 0.4 0.15 0.8 0.7 0.5 0.9 0.8 0.6 0.15 0.1 0.07 0.55 0.25 0.2 0.73 0.55 0.5 0.88 0.8 0.65 );
}

relation MountainFcst InsInMt { 
values= table (1.0 0.48 0.2 0.0 0.5 0.5 0.0 0.02 0.3 );
}

relation OutflowFrMt InsInMt WndHodograph { 
values= table (1.0 1.0 1.0 1.0 0.5 0.15 0.35 0.8 0.05 0.01 0.1 0.6 0.0 0.0 0.0 0.0 0.4 0.4 0.6 0.19 0.45 0.15 0.25 0.3 0.0 0.0 0.0 0.0 0.1 0.45 0.05 0.01 0.5 0.84 0.65 0.1 );
}

relation VISCloudCov { 
values= table (0.1 0.5 0.4 );
}

relation WindAloft Scenario { 
values= table (0.0 0.2 0.05 0.03 0.07 0.5 0.25 0.2 0.2 0.96 0.03 0.95 0.3 0.09 0.32 0.66 0.0 0.3 0.14 0.41 0.0 0.08 0.01 0.2 0.59 0.42 0.02 0.0 0.25 0.43 0.1 0.0 0.33 0.04 0.3 0.27 0.23 0.25 0.5 0.2 0.23 0.29 0.04 0.56 );
}

relation R5Fcst MountainFcst N34StarFcst { 
values= table (1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 1.0 1.0 1.0 1.0 );
}

relation ScenRel3_4 Scenario { 
values= table (1.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 0.0 0.0 );
}

relation CombClouds VISCloudCov IRCloudCover { 
values= table (0.95 0.85 0.8 0.45 0.1 0.05 0.1 0.02 0.0 0.04 0.13 0.1 0.52 0.8 0.45 0.4 0.28 0.02 0.01 0.02 0.1 0.03 0.1 0.5 0.5 0.7 0.98 );
}

relation CombVerMo N0_7muVerMo SubjVertMo QGVertMotion { 
values= table (1.0 0.9 0.7 0.2 0.9 0.7 0.15 0.1 0.7 0.15 0.2 0.1 0.2 0.1 0.1 0.1 0.9 0.7 0.15 0.1 0.7 0.0 0.0 0.0 0.15 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.7 0.15 0.2 0.1 0.15 0.0 0.0 0.0 0.2 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.2 0.1 0.1 0.1 0.1 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.0 0.1 0.2 0.5 0.1 0.3 0.7 0.35 0.2 0.7 0.6 0.2 0.5 0.35 0.2 0.1 0.1 0.3 0.7 0.35 0.3 1.0 0.7 0.2 0.7 0.7 0.3 0.15 0.35 0.2 0.15 0.1 0.2 0.7 0.6 0.2 0.7 0.7 0.3 0.15 0.6 0.3 0.0 0.0 0.2 0.15 0.0 0.0 0.5 0.35 0.2 0.1 0.35 0.2 0.15 0.1 0.2 0.15 0.0 0.0 0.1 0.1 0.0 0.0 0.0 0.0 0.1 0.2 0.0 0.0 0.15 0.45 0.1 0.15 0.2 0.6 0.2 0.45 0.6 0.2 0.0 0.0 0.15 0.45 0.0 0.0 0.3 0.7 0.15 0.3 0.7 0.5 0.45 0.7 0.5 0.2 0.1 0.15 0.2 0.6 0.15 0.3 0.7 0.5 0.2 0.7 1.0 0.7 0.6 0.5 0.7 0.3 0.2 0.45 0.6 0.2 0.45 0.7 0.5 0.2 0.6 0.5 0.7 0.3 0.2 0.2 0.3 0.0 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.1 0.1 0.1 0.1 0.6 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.35 0.1 0.1 0.35 0.7 0.0 0.0 0.0 0.1 0.0 0.0 0.0 0.35 0.0 0.0 0.0 0.3 0.1 0.35 0.3 0.7 0.1 0.1 0.1 0.6 0.1 0.1 0.35 0.7 0.1 0.35 0.3 0.7 0.6 0.7 0.7 1.0 );
}

}
