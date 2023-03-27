package org.chraibi.bot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import java.io.IOException;

import org.jsoup.nodes.Element;

public class ChraibiBot {
    static private LinkedHashMap<String, Integer> toVisit = new LinkedHashMap<>();
    static private HashSet<String> alreadyVisited = new HashSet<>();
    static private String urlCourant = null;
    static private Integer depthCourant;
    static private List<String> emails;

    static private HashSet<String> listeEmails = new HashSet<>();
    static private String listeFinale = "";
    static private ArrayList<String> namesList;
    static private String test;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Veuillez mettre 3 arguments");
            System.out.println("le premier argument doit être un nombre entier positif.");
            System.out.println("le deuxieme argument doit être un lien valide.");
            System.out.println("le troisième argument doit être un dossier qui existe.");
            System.exit(0);
        }
        if (!isNumericAndPositive(args[0])) {
            System.out.println("le premier argument doit être un nombre entier positif.");
            System.exit(0);

        }
        if (!validUrl(args[1])) {
            System.out.println("le deuxième argument doit être un url valide.");
            System.exit(0);

        }
        if (!fileExist(args[2])) {
            System.out.println("le troisième argument doit être un dossier qui existe.");
            System.exit(0);

        }
        getPageLinksTest(args[1], Integer.parseInt(args[0]));
    }
    public static void getPageLinksTest(String URL, int depth) {

        toVisit.put(URL, depth);
        System.out.println("Bonjour Maudie");
        System.out.println();
        System.out.println("Tout va bien, explorons");
        while (toVisit.size() != 0) {
            try {
                // information sur le PROCHAIN dans ma liste à vister
                urlCourant = getFirstKey(toVisit);
                depthCourant = getFirstValue(toVisit);

                // Map< k , v >


                // connect to the URL donne en parametre¸ON SE CONNECT
                Document document = Jsoup.connect(urlCourant).get();
                System.out.println(">> Exploration de " + urlCourant);


                //Nous somme mtn connecte, donc nous ajoutons a la liste alreadyVisited
                toVisit.remove(urlCourant);     //  ON S'OCCUPE DE LUI , DONC IL N'EST PLUS DANS LA FILE D'ATTENTE
                alreadyVisited.add(urlCourant); // ON NE VEUT PAS RE VISITER LA PAGE AU FUTUR


                //extraire les liens.. et les rajoute dans la fille d'attente. // On veut seulement faire sa s'il nous reste du carburant(depht)
                if (depthCourant != 0) {
                    Elements linksOnPage = document.select("a");

                    for (Element baliseA : linksOnPage) {
                        // converti baliseA en lien href
                        //String lien_absolu = baliseA.attr("abs:href");

                        // ajoute a la file d'attente SI n'a jamais ete visite
                        if (baliseA.attr("abs:href") != "") {
                            if (!alreadyVisited.contains(baliseA.attr("abs:href"))) {
                                toVisit.put(baliseA.attr("abs:href"), depthCourant - 1);
                            }
                            //toVisit.add(page.attr("abs:href"));
                        } else if (baliseA.attr("href") != "") {

                            if (!alreadyVisited.contains(baliseA.attr("href"))) {
                                toVisit.put(baliseA.attr("href"), depthCourant - 1);
                            }
                        }
                    }
                }
                emails = extraireEmail(document);
                for (int i = 0; i < emails.size(); i++) {
                    listeEmails.add(emails.get(i));
                }
                namesList = new ArrayList<>(listeEmails);

                String url = urlCourant;
                url = url.substring(url.lastIndexOf("/") + 1);
                createFile("./temp/" + url);
                if (!url.equals("")) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter("./temp/" + url));
                    bw.write(test);
                    bw.close();
                }

            } catch (IOException e) {
                System.out.println("Page inaccessible " + urlCourant);
                toVisit.remove(urlCourant);

            } catch (IllegalArgumentException e) {
                System.out.println("URL mal formé " + urlCourant);
                toVisit.remove(urlCourant);
            }
        }
        System.out.println();
        System.out.println("Nombre de pages explorées : " + alreadyVisited.size());


        Collections.sort(namesList, String.CASE_INSENSITIVE_ORDER);

        String Newligne = System.getProperty("line.separator");
        for (int i = 0; i < namesList.size(); i++) {
            listeFinale += namesList.get(i) + Newligne;
        }
        System.out.println("Nombre de courriels extraits (en ordre alphabétique) : " + namesList.size());
        System.out.println(listeFinale);
    }
    private static String getFirstKey(LinkedHashMap<String, Integer> linkedList) {
        int count = 1;
        for (Map.Entry<String, Integer> element :
                linkedList.entrySet()) {
            if (count == 1) {
                return element.getKey();
            }
            count++;
        }
        return null;
    }

    private static Integer getFirstValue(LinkedHashMap<String, Integer> linkedList) {

        int count = 1;
        for (Map.Entry<String, Integer> element :
                linkedList.entrySet()) {
            if (count == 1) {

                return element.getValue();
            }
            count++;
        }
        return null;
    }
    private static List<String> extraireEmail(Document document) {
        // System.out.println(document.toString());
        List<String> emails = new ArrayList<String>();
        Pattern p = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        Matcher matcher = p.matcher(document.text());
        test = document.toString();
        while (matcher.find()) {
            if (!emails.contains(matcher.group())) {
                emails.add(matcher.group());
                test = test.replace(matcher.group(), "Correc@ti.on");
            }
        }
        return emails;
    }
    private static void createFile(String path) {
        try {
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public static boolean isNumericAndPositive(String strNum) {

        try {
            int numero = Integer.parseInt(strNum);
            if (numero < 0) {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    private static boolean validUrl(String url) {

        try {
            URL obj = new URL(url);
            obj.toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }
    private static boolean fileExist(String path) {
        String strPath = path;
        // converts string to path
        Path p1 = Paths.get(strPath);
        if (Files.exists(p1)) {
            return true;
        }
        return false;
    }
}

