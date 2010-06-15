% ws - random walk sequence data
% wf - random walk failure data
% rs - randoop sequence data
% rf - randoop sequence data
% f - index of failure to analyze
function out = analyze(W, R, F, wlow, whigh)
ws = W.s; wf = W.f; wn = W.n;
rs = R.s; rf = R.f; rn = R.n;
% check: *s vars are row vectors with the same size.
assert(size(ws,1)==1 && size(ws,2)>1);
assert(size(rs,1)==1 && size(rs,2)>1);
assert(size(F.s,1)==1 && size(F.s,2)>1);
assert(size(ws,1)==size(rs,1) && size(ws,2)==size(rs,2));
assert(size(ws,1)==size(F.s,1) && size(ws,2)==size(F.s,2));
% check: *f are matrices with same cols as *s vectors.
assert(size(wf,1)>0 && size(wf,2)==size(wf,2));
assert(size(rf,1)>0 && size(rf,2)==size(rf,2));
assert(size(F.f,1)>0 && size(F.f,2)==size(F.f,2));
% check: *n are row vectors, length is #cols in *f vectors.
assert(size(wn,1)==1 && size(wn,2)==size(wf,1));
assert(size(rn,1)==1 && size(rn,2)==size(rf,1));
assert(size(F.n,1)==1 && size(F.n,2)==size(F.f,1));
% check: wlow and whigh
assert(wlow > 0 && wlow <= whigh);

% wf2 = wf; wn2 = wn;
[wf2,wn2] = restrict_failures_bymatch(wf,wn,'StatementThrowsNPE');

% disp('Total failures for random walk:')
% size(wf2,1)
% disp('Total failures for randoop0:')
% size(restrict_failures_bymatch(rf,rn,'StatementThrowsNPE'),1)
% disp('Total failures for randoop:')
% size(restrict_failures_bymatch(F.f,F.n,'StatementThrowsNPE'),1)

%rf2 = R.f; rn2 = R.n;
%ff2 = F.f; fn2 = F.n;
[rf2 rn2] = restrict_failures_bymatch(R.f,R.n,'StatementThrowsNPE');
[ff2 fn2] = restrict_failures_bymatch(F.f,F.n,'StatementThrowsNPE');

[wf3,wn3] = restrict_failures(wf2,wn2,wlow,whigh);
[rf3,rn3] = restrict_failures(rf2,rn2,wlow,whigh);
[ff3,fn3] = restrict_failures(ff2,fn2,wlow,whigh);
[s e]=sprintf('%d\t%d\t%d',size(wn3,2),size(rn3,2),size(fn3,2));
disp(s);
printfnames(wf2,wn2)

% disp('All random walk occurrences for different failures');
% sum(wf2,2)
% disp('All randoop0 occurrences for different failures');
% sum(rf,2)
% disp('All randoop occurrences for different failures');
% sum(F.f,2)

% wf2 contains the vector of failure counts per size.
% wn2 contains the failure counts for random walk.
[wf2,wn2] = restrict_failures(wf2,wn2,wlow,whigh);


% Use wn2 to determine failure counts for Randoop.
% rf2 contains the failures counts for randoop.
rf2 = restrict_failures_byname(rf,rn,wn2);

% Use wn2 to determine failure counts for Randoop.
% rf2 contains the failures counts for randoop.
ff2 = restrict_failures_byname(F.f,F.n,wn2);

disp('Final random walk occurrences for different failures');
sum(wf2)
disp('Final randoop0 occurrences for different failures');
sum(rf2)
disp('Final randoop occurrences for different failures');
sum(ff2)

% Compute ops from number of sequences.
opsw = work(ws);
opsr = work(rs);
opsf = work(F.s);


% disp('total operations for random walk:');
% sum(opsw)
% disp('total operations for randoop0:');
% sum(opsr)
% disp('total operations for randoop:');
% sum(opsf)

assert(size(wf2,1)==1 && size(wf,2)==50);
assert(size(rf2,1)==1 && size(rf,2)==50);
assert(size(ff2,1)==1 && size(F.f,2)==50);

assert(size(opsw,1)==1 && size(opsw,2)==50);
assert(size(opsr,1)==1 && size(opsr,2)==50);
assert(size(opsf,1)==1 && size(opsf,2)==50);

ratiow = group(wf2 ./ opsw,5);
ratior = group(rf2 ./ opsr,5);
ratiof = group(ff2 ./ opsf,5);

out.p1 = ws;
out.p2 = rs;
out.p3 = F.s;
out.p4 = opsw(1:49);
out.p5 = opsr(1:49);
out.p6 = opsf(1:49);
out.p7 = ratiow;
out.p8 = ratior;
out.p9 = ratiof;
out.x = [ 1 2 3 4 5 6 7 8 9 10 ];
