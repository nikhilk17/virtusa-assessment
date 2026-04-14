import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// custom exception for low balance
class InSufficientFundsException extends Exception {
    public InSufficientFundsException(String msg) {
        super(msg);
    }
}

// stores each transaction info
class Transaction {
    String type;
    double amount;
    String description;
    String dateTime;

    public Transaction(String type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;

        // get current date time as string
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        this.dateTime = LocalDateTime.now().format(fmt);
    }
}

// handles all the account stuff - deposit withdraw spend etc
class Account {
    private String holder;
    private double balance;
    private ArrayList<Transaction> history;
    // private int txnCount = 0;  // was gonna track total but not needed rn

    public Account(String holder, double balance) {
        this.holder = holder;
        this.balance = balance;
        this.history = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    // add money
    public void deposit(double amt) {
        if (amt <= 0) {
            System.out.println("Invalid deposit amount!");
            return;
        }

        balance = balance + amt;
        addTransaction("Deposit", amt, "Money Added");
        System.out.println("Deposit successful!");
    }

    // take out money
    public void withdraw(double amt)
            throws InSufficientFundsException {

        if (amt <= 0) {
            throw new IllegalArgumentException("Invalid amount!");
        }

        if (amt > balance) {
            throw new InSufficientFundsException("Insufficient balance!");
        }

        balance = balance - amt;
        addTransaction("Withdraw", amt, "Cash Withdrawal");
        System.out.println("Withdrawal successful!");
    }

    // spend on something - like food uber etc
    public void spend(double amt, String desc)
            throws InSufficientFundsException {
        if (amt <= 0) {
            throw new IllegalArgumentException("Invalid amount!");
        }

        if (amt > balance) {
            throw new InSufficientFundsException("Insufficient balance!");
        }

        balance -= amt;
        addTransaction("Spend", amt, desc);
        System.out.println("Payment successful!");
    }

    // only keep last 5 transactions
    private void addTransaction(String type, double amt, String desc) {
        if (history.size() == 5) {
            history.remove(0);
        }
        history.add(new Transaction(type, amt, desc));
    }

    // print last 5 txns
    public void printMiniStatement() {
        System.out.println("\n------ Mini Statement ------");

        if (history.size() == 0) {
            System.out.println("No transactions yet.");
        } else {
            for (int i = 0; i < history.size(); i++) {
                Transaction t = history.get(i);
                System.out.println(
                    t.dateTime + " | " +
                    t.type + " | Rs." + t.amount + " | " +
                    t.description
                );
            }
        }

        System.out.println("Current Balance: Rs." + balance);
        System.out.println("-------------------------------");
    }
}

// main app
public class FinSafeApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Account acc = new Account("Nikhil", 1000);

        while (true) {
            System.out.println("\n==== FinSafe Menu ====");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Spend");
            System.out.println("4. Mini Statement");
            System.out.println("5. Check Balance");
            System.out.println("6. Exit");
            System.out.print("Choose option: ");

            int choice;

            try {
                choice = sc.nextInt();
                sc.nextLine(); // fix buffer issue

                if (choice == 1) {
                    System.out.print("Enter deposit amount: ");
                    double dep = sc.nextDouble();
                    acc.deposit(dep);

                } else if (choice == 2) {
                    System.out.print("Enter withdrawal amount: ");
                    double wd = sc.nextDouble();
                    acc.withdraw(wd);

                } else if (choice == 3) {
                    System.out.print("Enter amount to spend: ");
                    double amt = sc.nextDouble();
                    sc.nextLine();
                    // check before asking for description
                    if(amt > acc.getBalance() || amt <= 0) {
                        System.out.println("Invalid amount or Insufficient balance!");
                    } else {
                        System.out.print("Enter description (Food, Uber, etc): ");
                        String desc = sc.nextLine();
                        acc.spend(amt, desc);
                    }

                } else if (choice == 4) {
                    acc.printMiniStatement();

                } else if (choice == 5) {
                    double bal = acc.getBalance();
                    System.out.println(bal);

                } else if (choice == 6) {
                    System.out.println("Exiting FinSafe...");
                    sc.close();
                    return;

                } else {
                    System.out.println("Invalid choice!");
                }

            } catch (InSufficientFundsException e) {
                System.out.println(e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Invalid input!");
                sc.nextLine();
            }
        }
    }
}
