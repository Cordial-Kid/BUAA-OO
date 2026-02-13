import java.util.ArrayList;
import java.util.List;

public class Input {
    public static void exe(ArrayList<ArrayList<String>> inputInfo,
                           int n,
                           AdventureContainer roster
    ) {
        for (int i = 0; i < n; i++) {
            List<String> row = inputInfo.get(i);
            int j = 0;
            String cmd = row.get(j++);

            switch (cmd) {
                case "aa":
                    roster.addAdventurer(row.get(j));
                    break;
                case "ab":
                    String advIDab = row.get(j++);
                    String bottleID = row.get(j++);
                    String typeAb = row.get(j++);
                    int effect = Integer.parseInt(row.get(j));
                    roster.addBottleTop(advIDab, bottleID, typeAb, effect);
                    break;
                case "ae":
                    String advIDae = row.get(j++);
                    String equipmentID = row.get(j++);
                    String typeAe = row.get(j++);
                    int ce = Integer.parseInt(row.get(j));
                    roster.addEquipTop(advIDae, equipmentID, typeAe, ce);
                    break;
                case "ls":
                    String advIDls = row.get(j++);
                    String spellID = row.get(j++);
                    String typeLs = row.get(j++);
                    int manaCost = Integer.parseInt(row.get(j++));
                    int power = Integer.parseInt(row.get(j));
                    roster.learnSpellTop(advIDls, spellID, typeLs, manaCost, power);
                    break;
                case "ri":
                    roster.removeItemTop(row.get(j++), row.get(j));
                    break;
                case "ti":
                    roster.takeItemTop(row.get(j++), row.get(j));
                    break;
                case "use":
                    roster.use(row.get(j++), row.get(j++), row.get(j));
                    break;
                case "bi":
                    roster.buyItemTop(row.get(j++), row.get(j++), row.get(j));
                    break;
                case "ar":
                    roster.addRelationTop(row.get(j++), row.get(j));
                    break;
                case "rr":
                    roster.removeRelation(row.get(j++), row.get(j));
                    break;
                case "fight":
                    String advId = row.get(j++);
                    int number = Integer.parseInt(row.get(j++));
                    List<String> list = new ArrayList<>();
                    for (int t = 0; t < number; t++) {
                        list.add(row.get(j++));
                    }
                    roster.fightTop(advId, number, list);
                    break;
                default:
                    String input = row.get(j);
                    Lexer lexer = new Lexer(input);
                    String topEmployer = lexer.peek();
                    lexer.next();
                    lexer.match("(");
                    parseEmployees(lexer, topEmployer, roster);
                    lexer.match(")");
            }
        }
    }

    private static void parseEmployees(Lexer lexer, String employer, AdventureContainer roster) {
        while (true) {
            String token = lexer.peek();
            if (token == null || token.equals(")")) {
                break;
            }

            String employee = token;
            lexer.next();

            //建立该雇员和雇主的关系
            roster.addRelationTop(employer, employee);

            //该雇员也是雇主
            if ("(".equals(lexer.peek())) {
                lexer.match("(");
                parseEmployees(lexer, employee, roster);
                lexer.match(")");
            }

            //该雇员不是雇主，且存在下一个雇员
            if (",".equals(lexer.peek())) {
                lexer.match(",");
            }

        }
    }
}
