function plotdata2(out)

subplot(1,3,1);
area(out.p4, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('Random walk','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('operations performed', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);

subplot(1,3,2);
area(out.p5, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('DRT, legal seq. pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('operations performed', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);

subplot(1,3,3);
area(out.p6, 'FaceColor', [ 0.5 0.5 0.5 ]);
title('DRT, full pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('operations performed', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p4) max(out.p5) max(out.p6) ])]);
