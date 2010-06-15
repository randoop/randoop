om.s=zeros(1,50);
fc.s=zeros(1,50);
fd.s=zeros(1,50);

omjc_data;
omjc = normalizedata(omjc);
fcjc_data;
fcjc = normalizedata(fcjc);
fdjc_data;
fdjc = normalizedata(fdjc);

omcc_data;
omcc = normalizedata(omcc);
fccc_data;
fccc = normalizedata(fccc);
fdcc_data;
fdcc = normalizedata(fdcc);

ompr_data;
ompr = normalizedata(ompr);
fcpr_data;
fcpr = normalizedata(fcpr);
fdpr_data;
fdpr = normalizedata(fdpr);

% omjf_data;
% omjf = normalizedata(omjf);
% fcjf_data;
% fcjf = normalizedata(fcjf);
% fdjf_data;
% fdjf = normalizedata(fdjf);

omma_data;
omma = normalizedata(omma);
fcma_data;
fcma = normalizedata(fcma);
fdma_data;
fdma = normalizedata(fdma);

omtr_data;
omtr = normalizedata(omtr);
fctr_data;
fctr = normalizedata(fctr);
fdtr_data;
fdtr = normalizedata(fdtr);

lower = 100000;
fail = 1000000;

% p1 = analyze(fdjf,fcjf,omjf,lower,fail);
% p2 = analyze(fdjc,fcjc,omjc,lower,fail);
% p3 = analyze(fdcc,fccc,omcc,lower,fail);
% p4 = analyze(fdpr,fcpr,ompr,lower,fail);
% p5 = analyze(fdma,fcma,omma,lower,fail);
% p6 = analyze(fdtr,fctr,omtr,lower,fail);

% p1 = analyze(omjf,fcjf,fdjf,lower,fail);
 p2 = analyze(omjc,fcjc,fdjc,lower,fail);
 p3 = analyze(omcc,fccc,fdcc,lower,fail);
 p4 = analyze(ompr,fcpr,fdpr,lower,fail);
 p5 = analyze(omma,fcma,fdma,lower,fail);
 p6 = analyze(omtr,fctr,fdtr,lower,fail);

p = average([ p2 p3 p4 p5 p6 ]);

  plotdata1(p);
  figure;
  plotdata2(p);
  figure;
plotdata3(p);
disp('\\n')



