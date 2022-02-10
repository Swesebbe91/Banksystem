package se.sensera.banking.Implementation;

import se.sensera.banking.*;

import javax.swing.text.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class TransactionImpl implements Transaction {

    private String id;
    private String timeStamp;
    private User user;
    private Account account;
    private double amount;

    public TransactionImpl(String id, String timeStamp, User user, Account account, double amount) {
        this.id = id;
        this.timeStamp = timeStamp;
        this.user = user;
        this.account = account;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Date getCreated() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY);
        Date date = null;
        try {
            date = formatter.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Account getAccount() {
        return this.account;
    }

    @Override
    public double getAmount() {
        return this.amount;
    }
}
