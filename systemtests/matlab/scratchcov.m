om.s=zeros(1,50);
fc.s=zeros(1,50);
fd.s=zeros(1,50);

omjc_covdata;
omjc = normalizedata(omjc);
fcjc_covdata;
fcjc = normalizedata(fcjc);
fdjc_covdata;
fdjc = normalizedata(fdjc);

% omcc_covdata;
% omcc = normalizedata(omcc);
% fccc_covdata;
% fccc = normalizedata(fccc);
% fdcc_covdata;
% fdcc = normalizedata(fdcc);
% 
% ompr_covdata;
% ompr = normalizedata(ompr);
% fcpr_covdata;
% fcpr = normalizedata(fcpr);
% fdpr_covdata;
% fdpr = normalizedata(fdpr);

% omjf_covdata;
% omjf = normalizedata(omjf);
% fcjf_covdata;
% fcjf = normalizedata(fcjf);
% fdjf_covdata;
% fdjf = normalizedata(fdjf);
% 
% omma_covdata;
% omma = normalizedata(omma);
% fcma_covdata;
% fcma = normalizedata(fcma);
% fdma_covdata;
% fdma = normalizedata(fdma);
% 
% omtr_covdata;
% omtr = normalizedata(omtr);
% fctr_covdata;
% fctr = normalizedata(fctr);
% fdtr_covdata;
% fdtr = normalizedata(fdtr);

lower = 1;
fail = 100;

% p1 = analyze(fdjf,fcjf,omjf,lower,fail);
% p2 = analyze(fdjc,fcjc,omjc,lower,fail);
% p3 = analyze(fdcc,fccc,omcc,lower,fail);
% p4 = analyze(fdpr,fcpr,ompr,lower,fail);
% p5 = analyze(fdma,fcma,omma,lower,fail);
% p6 = analyze(fdtr,fctr,omtr,lower,fail);

% p1 = analyze(omjf,fcjf,fdjf,lower,fail);
 p2 = analyze(omjc,fcjc,fdjc,lower,fail);
%  p3 = analyze(omcc,fccc,fdcc,lower,fail);
%  p4 = analyze(ompr,fcpr,fdpr,lower,fail);
%  p5 = analyze(omma,fcma,fdma,lower,fail);
%  p6 = analyze(omtr,fctr,fdtr,lower,fail);

p = average([ p2 p2 ]);

  plotdata1(p);
  figure;
  plotdata2(p);
  figure;
plotdata3(p);
disp('\\n')



