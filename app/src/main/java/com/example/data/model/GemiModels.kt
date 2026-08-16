package com.example.data.model

enum class PipelineStatus(val labelGr: String, val labelEn: String) {
    NEW("Νέο Lead", "New Lead"),
    CONTACTED("Έγινε Επαφή", "Contacted"),
    MEETING_SET("Ραντεβού", "Meeting Set"),
    PROPOSAL_SENT("Προσφορά", "Proposal Sent"),
    DEAL_WON("Κερδισμένο (Won)", "Deal Won"),
    NOT_INTERESTED("Ανεπιθύμητο", "Not Interested")
}

data class KadCategory(
    val id: String,
    val nameGr: String,
    val icon: String,
    val defaultKads: List<String>,
    val descriptionGr: String
)

data class KadItem(
    val code: String,
    val titleGr: String,
    val categoryId: String
)

object GreekTaxonomy {
    val CATEGORIES = listOf(
        KadCategory(
            id = "HORECA",
            nameGr = "Εστίαση & HORECA",
            icon = "restaurant",
            defaultKads = listOf("56.10", "56.30", "56.21", "56.29"),
            descriptionGr = "Εστιατόρια, Ταβέρνες, Beach Bars, Καφέ, Catering & Υπηρεσίες Τροφοδοσίας"
        ),
        KadCategory(
            id = "TOURISM",
            nameGr = "Τουρισμός & Ξενοδοχεία",
            icon = "hotel",
            defaultKads = listOf("55.10", "55.20", "79.11", "79.12"),
            descriptionGr = "Ξενοδοχεία, Βίλες, Ενοικιαζόμενα Δωμάτια, Τουριστικά Πρακτορεία"
        ),
        KadCategory(
            id = "RETAIL",
            nameGr = "Λιανικό & Χονδρικό Εμπόριο",
            icon = "storefront",
            defaultKads = listOf("47.11", "47.71", "47.91", "46.90"),
            descriptionGr = "Supermarkets, Ενδύματα, E-commerce καταστήματα, Χονδρικό Εμπόριο"
        ),
        KadCategory(
            id = "CONSTRUCTION",
            nameGr = "Κατασκευές & Οικοδομή",
            icon = "construction",
            defaultKads = listOf("41.20", "43.21", "43.22", "68.31"),
            descriptionGr = "Κατασκευαστικές, Ηλεκτρολογικές & Υδραυλικές εγκαταστάσεις, Real Estate"
        ),
        KadCategory(
            id = "TECH",
            nameGr = "Τεχνολογία & Digital",
            icon = "computer",
            defaultKads = listOf("62.01", "62.02", "73.11", "63.11"),
            descriptionGr = "Software, IT Consulting, Digital Marketing, Web Development & Hosting"
        ),
        KadCategory(
            id = "SERVICES",
            nameGr = "Υπηρεσίες & Λογιστικά",
            icon = "account_balance",
            defaultKads = listOf("69.20", "70.22", "69.10", "74.90"),
            descriptionGr = "Λογιστικά Γραφεία, Συμβουλευτικές Επιχειρήσεων, Νομικές Υπηρεσίες"
        ),
        KadCategory(
            id = "HEALTH",
            nameGr = "Υγεία & Ευεξία",
            icon = "spa",
            defaultKads = listOf("86.90", "96.02", "86.21", "93.13"),
            descriptionGr = "Ιατρεία, Φυσικοθεραπεία, Κέντρα Αισθητικής, Κομμωτήρια, Γυμναστήρια"
        )
    )

    val ALL_KADS = listOf(
        KadItem("56.10", "Υπηρεσίες εστιατορίων & ταβερνών", "HORECA"),
        KadItem("56.30", "Υπηρεσίες παροχής ποτών (Καφέ, Μπαρ, Beach Bar)", "HORECA"),
        KadItem("56.21", "Δραστηριότητες παροχής υπηρεσιών catering εκδηλώσεων", "HORECA"),
        KadItem("56.29", "Άλλες υπηρεσίες εστίασης & τροφοδοσίας", "HORECA"),
        
        KadItem("55.10", "Ξενοδοχεία και παρόμοια καταλύματα", "TOURISM"),
        KadItem("55.20", "Καταλύματα διακοπών & σύντομης διαμονής (Villas, BnB)", "TOURISM"),
        KadItem("79.11", "Δραστηριότητες ταξιδιωτικών πρακτορείων", "TOURISM"),
        KadItem("79.12", "Δραστηριότητες γραφείων οργανωμένων ταξιδιών", "TOURISM"),

        KadItem("47.11", "Λιανικό εμπόριο σε mini market, super market & παντοπωλεία", "RETAIL"),
        KadItem("47.71", "Λιανικό εμπόριο ενδυμάτων & υποδημάτων", "RETAIL"),
        KadItem("47.91", "Λιανικό εμπόριο μέσω διαδικτύου (E-shop)", "RETAIL"),
        KadItem("46.90", "Μη εξειδικευμένο χονδρικό εμπόριο", "RETAIL"),

        KadItem("41.20", "Κατασκευή οικιστικών και μη οικιστικών κτιρίων", "CONSTRUCTION"),
        KadItem("43.21", "Ηλεκτρικές εγκαταστάσεις & φωτισμός", "CONSTRUCTION"),
        KadItem("43.22", "Υδραυλικές εγκαταστάσεις & κλιματισμός / θέρμανση", "CONSTRUCTION"),
        KadItem("68.31", "Μεσιτικά γραφεία ακινήτων (Real Estate)", "CONSTRUCTION"),

        KadItem("62.01", "Δραστηριότητες προγραμματισμού ηλεκτρονικών συστημάτων", "TECH"),
        KadItem("62.02", "Υπηρεσίες συμβουλών πληροφορικής (IT Consulting)", "TECH"),
        KadItem("73.11", "Διαφημιστικά γραφεία & Digital Marketing Agencies", "TECH"),
        KadItem("63.11", "Επεξεργασία δεδομένων, φιλοξενία ιστοσελίδων (Cloud/Hosting)", "TECH"),

        KadItem("69.20", "Δραστηριότητες λογιστικής, τήρησης βιβλίων & φοροτεχνικών", "SERVICES"),
        KadItem("70.22", "Δραστηριότητες παροχής επιχειρηματικών συμβουλών", "SERVICES"),
        KadItem("69.10", "Νομικές δραστηριότητες", "SERVICES"),
        
        KadItem("86.90", "Άλλες δραστηριότητες ανθρώπινης υγείας (Φυσικοθεραπεία κ.α.)", "HEALTH"),
        KadItem("96.02", "Κομμωτήρια, κουρεία & ινστιτούτα αισθητικής", "HEALTH"),
        KadItem("93.13", "Εγκαταστάσεις γυμναστικής (Gyms & Fitness)", "HEALTH")
    )
}

object GreekRegions {
    val PAN_HELLENIC = "Πανελλαδικά (Όλη η Ελλάδα)"

    val REGIONS = listOf(
        "Αττική",
        "Κεντρική Μακεδονία",
        "Κρήτη",
        "Νότιο Αιγαίο (Κυκλάδες / Δωδεκάνησα)",
        "Δυτική Ελλάδα",
        "Θεσσαλία",
        "Ιόνια Νησιά",
        "Πελοπόννησος",
        "Ήπειρος",
        "Ανατολική Μακεδονία & Θράκη",
        "Στερεά Ελλάδα",
        "Βόρειο Αιγαίο",
        "Δυτική Μακεδονία"
    )

    val PREFECTURES_MAP = mapOf(
        "Αττική" to listOf("Κεντρικός Τομέας Αθηνών", "Βόρειος Τομέας Αθηνών", "Νότιος Τομέας Αθηνών", "Πειραιάς", "Ανατολική Αττική", "Δυτική Αττική"),
        "Κεντρική Μακεδονία" to listOf("Θεσσαλονίκη", "Χαλκιδική", "Σέρρες", "Πιερία", "Ημαθία", "Κιλκίς", "Πέλλα"),
        "Κρήτη" to listOf("Ηράκλειο", "Χανιά", "Ρέθυμνο", "Λασίθι"),
        "Νότιο Αιγαίο (Κυκλάδες / Δωδεκάνησα)" to listOf("Μύκονος", "Σαντορίνη (Θήρα)", "Πάρος", "Νάξος", "Σύρος", "Ρόδος", "Κως"),
        "Δυτική Ελλάδα" to listOf("Αχαΐα (Πάτρα)", "Αιτωλοακαρνανία", "Ηλεία"),
        "Θεσσαλία" to listOf("Λάρισα", "Μαγνησία (Βόλος)", "Τρίκαλα", "Καρδίτσα"),
        "Ιόνια Νησιά" to listOf("Κέρκυρα", "Ζάκυνθος", "Κεφαλονιά", "Λευκάδα"),
        "Πελοπόννησος" to listOf("Μεσσηνία (Καλαμάτα)", "Κορινθία", "Αργολίδα (Ναύπλιο)", "Αρκαδία (Τρίπολη)", "Λακωνία")
    )
}

object GreekLegalForms {
    val ALL = listOf("ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική")
}
