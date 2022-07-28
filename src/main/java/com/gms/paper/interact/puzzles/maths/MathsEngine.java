package com.gms.paper.interact.puzzles.maths;

import com.gms.mc.interact.puzzles.MathsTopic;
import com.gms.mc.util.Log;
import io.netty.util.internal.ThreadLocalRandom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.gms.mc.interact.puzzles.maths.MathsEngine.Operator.*;
import static com.gms.paper.interact.puzzles.maths.MathsEngine.Operator.*;

public class MathsEngine {

    public enum Operator {
        PLUS('+', 2147483647),
        MINUS('-', 2147483646),
        DIVIDE('÷', 2147483644),
        MULTIPLY('×', 2147483645),
        EQUALS('=', 2147483643),
        QUESTION_MARK('?', 2147483642);

        public char symbol;
        public int value;

        Operator(char symbol, int value) {
            this.symbol = symbol;
            this.value = value;
        }
    }

    private final Random random = new Random();

    //TODO: These values are default - different paradigms should be configurable, including default
    private Map<String, Boolean> options = new HashMap<>()
    {{
        put("addQuestionMark", false);
        put("addAnswer", false);
    }};
    private final Map<String, Object> rules = new HashMap<>()
    {{
        put("allowNegatives", false);
        put("zeroAversion", 5);
        put("oneAversion", 3);
        put("sameAversion", 0);
    }};

    private Operator[] operators;

    public MathsTopic getMode() {
        return mode;
    }

    public MathsEngine setMode(MathsTopic mode) {
        this.mode = mode;
        return this;
    }

    private MathsTopic mode;

    MathsEngine(MathsTopic mode){
        this.mode = mode;

        switch (this.mode) {
            //TODO: This default configuration should be made into a template
            case ADDITION -> {
                this.operators = new Operator[] {PLUS, EQUALS};
            }
            case SUBTRACTION -> {
                this.operators = new Operator[] {MINUS, EQUALS};
            }
        }

    }

    public boolean isAddQuestionMark() {
        return (boolean) this.options.get("addQuestionMark");
    }

    public MathsEngine setAddQuestionMark(boolean addQuestionMark) {
        this.options.replace("addQuestionMark", addQuestionMark);
        return this;
    }

    public boolean isAddAnswer() {
        return (boolean) this.options.get("addAnswer");
    }

    public MathsEngine setAddAnswer(boolean addAnswer) {
        this.options.replace("addAnswer", addAnswer);
        return this;
    }

    public int[] generateSum(int arg) {
        return this.generateSum(arg, false);
    }

    private int[] generateSum(int arg, boolean bypassOptions) {
        //TODO: Definitely get rid of this arg crap ASAP (it currently serves as a max bound for TOWERs' increasing difficulty)
        //TODO: This only caters to Tower puzzles right now, will have to be much more modular
        //TODO: This sum doesn't contain an answer value - may want to change that?
        int[] sum = new int[operators.length * 2];
        int total;

        while (true) {

            /// -GENERATION-
            for (int i = 0 ; i < this.operators.length * 2 ; i = i + 2) {
                sum[i] = this.generateInteger(0, arg);
                sum[i+1] = this.operators[i == 0 ? 0 : i/2].value;
            }

            /// -SUMMATION-
            total = sum[0];
            for (int i = 0 ; i < this.operators.length * 2 ; i = i + 2) {
                switch (this.operators[i == 0 ? 0 : i/2]) {
                    case PLUS -> total = total + sum[i+2];
                    case MINUS -> total = total - sum[i+2];
                    case MULTIPLY -> total = total * sum[i+2];
                    case DIVIDE -> total = total / sum[i+2];
                    case EQUALS -> {}
                    default -> Log.error("Not functionality yet implemented for " + this.operators[i == 0 ? 0 : i/2].symbol + " at index " + i + " in MathsEngine.generateSum."); //TODO: Check this error message index is correct
                }
            }

            /// -RULES-

            // No negatives rule
            if (!(boolean) this.rules.get("allowNegatives") && total < 0)  continue;

            /// - AVERSION RULES -
            /// 10 = always avoid, 0 = no aversion.
            // Zero-aversion rule
            int zeroAversion = (int) this.rules.get("zeroAversion");
            if (total == 0 && zeroAversion > 0) {
                int zeroCatch = ThreadLocalRandom.current().nextInt(0, 10);
                if (zeroCatch >= zeroAversion) continue;
            }
            // One-aversion rule
            int oneAversion = (int) this.rules.get("oneAversion");
            if (total == 0 && oneAversion > 0) {
                int oneCatch = ThreadLocalRandom.current().nextInt(0, 10);
                if (oneCatch >= oneAversion) continue;
            }
            // Same-aversion rule
            int sameAversion = (int) this.rules.get("sameAversion");
            if (total == 0 && sameAversion > 0) {
                int sameCatch = ThreadLocalRandom.current().nextInt(0, 10);
                if (sameCatch >= sameAversion) continue;
            }

            break;
        }

        return this.addOptions(sum, total);
    }

    //TODO: again, scrap the arg
    public int[][] generateSumStream(int length, int arg) {

        Map<String, Boolean> cachedOptions = this.options.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (String option : this.options.keySet()) this.options.replace(option, false);
        this.setAddAnswer(true);

        int[][] sumStream = new int[length][];
        for (int i = 0 ; i < length ; i++) {
            sumStream[i] = this.generateSum(arg);
        }

        HashMap<Integer, Integer> totals = new HashMap<>();
        for (int i = 0 ; i < sumStream.length ; i++) {
            totals.put(i, sumStream[i][sumStream[i].length-1]);
        }

        int sameAversion = this.getSameAversion();
        if (sameAversion > 0) {

            HashMap<Object, Object> duplicates = this.getDuplicateMapValues(totals);
            while (duplicates.size() > 0) {

                int sameCatch = ThreadLocalRandom.current().nextInt(0, 10);
                if (sameCatch >= sameAversion) break;

                int first = (int) duplicates.keySet().iterator().next();
                sumStream[first] = this.generateSum(arg);
                totals.put(first, sumStream[first][sumStream[first].length-1]);
                duplicates = this.getDuplicateMapValues(totals);

            }
        }

        this.options = cachedOptions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (int i = 0 ; i < sumStream.length; i++) {
            sumStream[i] =  this.addOptions(Arrays.copyOf(sumStream[i], sumStream[i].length-1), totals.get(i));
        }

        return sumStream;
    }

    private int[] addOptions(int[] sum, int total) {

        int [] newSum = null;
        for (Map.Entry<String, Boolean> option : this.options.entrySet()) {
            if (option.getValue()) {
                newSum = Arrays.copyOf(sum, sum.length + 1);
                switch (option.getKey()) {
                    case "addQuestionMark" -> newSum[newSum.length-1] = QUESTION_MARK.value;
                    case "addAnswer" -> newSum[newSum.length-1] = total;
                }
            }
        }

        return newSum == null ? sum : newSum;

    }

//    public static int[] doSum(Player player, String sum, boolean reset) throws InvalidFrameWriteException {
//
//        int elementsAmount = 0; // Must include elements that don't change (e.g. =)
//        int[] elements = new int[0];
//        boolean override = false;
//
//        switch (sum) { //TODO: Pull these sums in from elsewhere (database?) and make array assignment modular
//            case "Sum1" -> { // n + n = n
//                elementsAmount = 5;
//                elements = new int[elementsAmount];
//                elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                /**
//                 * 0 = +
//                 * 1 = -
//                 * 2 = ×
//                 * 3 = ÷
//                 * 4 = =
//                 */
//                elements[1] = ThreadLocalRandom.current().nextInt(0, 3 + 1);
//                elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                elements[3] = 4;
//                switch (elements[1]) {
//                    case 0 -> elements[4] = (elements[0] + elements[2]);
//                    case 1 -> {
//                        elements[4] = (elements[0] - elements[2]);
//                        do {
//                            elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                            elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                            elements[4] = (elements[0] - elements[2]);
//                        } while (elements[4] < 0);
//                    }
//                    case 2 -> elements[4] = (elements[0] * elements[2]);
//                    case 3 -> {
//                        float div;
//                        do {
//                            elements[0] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                            elements[2] = ThreadLocalRandom.current().nextInt(0, 9 + 1);
//                            div = ((float) elements[0]) / ((float) elements[2]);
//                        } while (div % 1 != 0);
//                        elements[4] = (elements[0] / elements[2]);
//                    }
//
//                }
//                elements[1] = (2147483647 - elements[1]); //TODO: Operator handling
//                elements[3] = (2147483647 - elements[3]);
//            }
//            case "Sum2" -> { // n + n = n
//
//                elementsAmount = 5;
//                elements = new int[elementsAmount];
//                elements[0] = ThreadLocalRandom.current().nextInt(0, 4 + 1);
//                /**
//                 * 0 = +
//                 * 1 = -
//                 * 2 = ×
//                 * 3 = ÷
//                 * 4 = =
//                 * 5 = x
//                 */
//                elements[1] = 0;
//                elements[2] = ThreadLocalRandom.current().nextInt(0, 5 + 1);
//                elements[3] = 4;
//                elements[4] = 5;
//                elements[1] = (2147483647 - elements[1]);
//                elements[3] = (2147483647 - elements[3]);
//                elements[4] = (2147483647 - elements[4]);
//                sumX = (elements[0] + elements[2]);
//                solveForX = true;
//            }
//            case "NSR-1" -> { // n + n = n
//
//                elementsAmount = 5;
//                elements = new int[elementsAmount];
//                elements[0] = ThreadLocalRandom.current().nextInt(0, 10 + 1);
//                /**
//                 * 0 = +
//                 * 1 = -
//                 * 2 = ×
//                 * 3 = ÷
//                 * 4 = =
//                 * 5 = x
//                 */
//                elements[1] = 0;
//                elements[2] = ThreadLocalRandom.current().nextInt(0, 10 + 1);
//                elements[3] = 4;
//                elements[4] = 5;
//                elements[1] = (2147483647 - elements[1]);
//                elements[3] = (2147483647 - elements[3]);
//                elements[4] = (2147483647 - elements[4]);
//                override = true;
//            }
//
//            case "TWR-1" -> {
//                int level = Integer.parseInt(sum.replace("TWR-1",""));
//                elementsAmount = 5;
//                elements = new int[elementsAmount];
//                elements[0] = ThreadLocalRandom.current().nextInt(0, 5 + level); // Default difficulty - ground floor, both numbers <= 5, this goes up by one per floor
//                /**
//                 * 0 = +
//                 * 1 = -
//                 * 2 = ×
//                 * 3 = ÷
//                 * 4 = =
//                 * 5 = x
//                 */
//                elements[1] = 0;
//                elements[2] = ThreadLocalRandom.current().nextInt(0, 5 + level);
//                elements[3] = 4;
//                elements[4] = 5;
//                elements[1] = (2147483647 - elements[1]);
//                elements[3] = (2147483647 - elements[3]);
//                elements[4] = (2147483647 - elements[4]);
//            }
//
//        }
//        if (reset) {
//            Arrays.fill(elements,2147483642);
//        }
//        if ((elements.length > 0) && !override) {
//            for (int i = 1; i <= elementsAmount; i++) {
//                findFrame(player, sum, i, elements[(i - 1)], 1);
//            }
//        }
//        return elements;
//    }

    //TODO: This shouldn't take arguments like this
    public int generateInteger(int min, int max) {
        //TODO: Script exception for this (IF WE NEED THIS AT ALL?)
        if (min == max) {
            Log.error("MATHS_ENGINE: Generate integer range must be greater than 0.");
            return -1;
        }
        int number;
        boolean redo = true;

        if ((boolean) this.rules.get("allowNegatives")){
            //TODO: This is where code would be to inform this rule - right now arguments make it redundant
        }

        //TODO: Number cannot be generate until all rules have been satisfied
        //TODO: The format of the below system is fairly brute-forcey - there may well be a better way to accomplish this
        //TODO: Also worth working with different random methods, bearing streams in mind

        while (true) {

            number = ThreadLocalRandom.current().nextInt(min, max + 1);

            // Zero-aversion rule
            int zeroAversion = (int) this.rules.get("zeroAversion");
            if (number == 0 && zeroAversion > 0) {
                int zeroCatch = ThreadLocalRandom.current().nextInt(min, max);
                if (zeroCatch >= zeroAversion) continue;
            }

            break;
        }

        return number;

    }

    public int solveSum(int[] sum) {
        int answer = sum[0];
        for (int i = 1 ; i < sum.length ; i++) {
            for (Operator operator : Operator.values()) {
                if (sum[i] == operator.value) {
                    switch (operator) {
                        case PLUS -> answer = answer + sum[i+1];
                        case MINUS -> answer = answer - sum[i+1];
                        case MULTIPLY -> answer = answer * sum[i+1];
                        case DIVIDE -> answer = answer / sum[i+1];
                        case EQUALS, QUESTION_MARK -> {}
                    }
                }
            }
        }
        return answer;
    }

    private HashMap<Object, Object> getDuplicateMapValues(HashMap<?, ?> hashMap) {
        hashMap = (HashMap<?, ?>)hashMap.clone();
        HashMap<Object, Object> duplicates = new HashMap<>();
        Object[] keys = hashMap.keySet().toArray();

        for (Object key : keys) {
            Object value = hashMap.get(key);
            hashMap.remove(key);
            if (hashMap.containsValue(value)) {
                duplicates.put(key, value);
            }
            if (duplicates.containsValue(value)) {
                duplicates.put(key, value);
            }
        }

        return(duplicates);
    }

    public boolean isAllowNegatives() {
        return (boolean) this.rules.get("allowNegatives");
    }

    public int getZeroAversion() {
        return (int) this.rules.get("zeroAversion");
    }

    public MathsEngine setZeroAversion(int zeroAversion) {
        this.rules.replace("zeroAversion", zeroAversion);
        return this;
    }

    public int getOneAversion() {
        return (int) this.rules.get("oneAversion");
    }

    public MathsEngine setOneAversion(int oneAversion) {
        this.rules.replace("oneAversion", oneAversion);
        return this;
    }

    public int getSameAversion() {
        return (int) this.rules.get("sameAversion");
    }

    public MathsEngine setSameAversion(int sameAversion) {
        this.rules.replace("sameAversion", sameAversion);
        return this;
    }

    public MathsEngine setAllowNegatives(boolean allowNegatives) {
        this.rules.replace("allowNegatives", allowNegatives);
        return this;
    }

}
