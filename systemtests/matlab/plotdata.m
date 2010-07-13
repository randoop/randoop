function plotdata(out)

subplot(3,3,1);
plot(out.p1,'Color','black','LineWidth',2);
title('Sequence distribution');
xlabel('length');ylabel('sequences');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);

subplot(3,3,2);
plot(out.p2,'Color','black','LineWidth',2);
title('Sequence distribution');
xlabel('length');ylabel('sequences');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);

subplot(3,3,3);
plot(out.p3,'Color','black','LineWidth',2);
title('Sequence distribution');
xlabel('length');ylabel('sequences');
xlim([0 49]);
ylim([0 max([ max(out.p1) max(out.p2) max(out.p3) ])]);

subplot(3,3,4);
area(out.p4, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('Operations distribution');
xlabel('length');ylabel('operations');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);

subplot(3,3,5);
area(out.p5, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('Operations distribution');
xlabel('length');ylabel('operations');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);

subplot(3,3,6);
area(out.p6, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('Operations distribution');
xlabel('length');ylabel('operations');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);

subplot(3,3,7); bar(out.x,out.p7,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('Failure ratio');
xlabel('length');ylabel('failures/operations');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);

subplot(3,3,8); bar(out.x, out.p8,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('Failure ratio');
xlabel('length');ylabel('failures/operations');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);

subplot(3,3,9); bar(out.x, out.p9,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('Failure ratio');
xlabel('length');ylabel('failures/operations');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);
