function plotdata3(out)

subplot(1,3,1);
bar(out.x,out.p7,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('Random walk','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('failures/operation', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);

subplot(1,3,2);
bar(out.x, out.p8,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('DRT, legal seq. pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('failures/operation', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);

subplot(1,3,3);
bar(out.x, out.p9,  'FaceColor', [ 0.5 0.5 0.5 ]);
title('DRT, full pruning','FontSize',14,'FontName','Arial');
xlabel('sequence length', 'FontSize',12,'FontName','Arial');
ylabel('failures/operation', 'FontSize',12,'FontName','Arial');
ylim([0 max([ max(out.p7) max(out.p8) max(out.p9) ])]);
