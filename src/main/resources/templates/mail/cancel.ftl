<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Paiement rejeté</title>
</head>
<body style="line-height: 1.8">
    <p>Bonjour <b><i>${name}</i></b>,</p>
    <p>Le paiement que vous avez initié a été annulé pour le motif : <b>${motif}</b>.</p>
    <p>Détail du paiement :</p>
    <ul>
        <li>Montant Total : <b><i>${amount} FCFA</i></b></li>
        <li>Caution : <i>${caution} FCFA</i></li>
        <li>Frais de reparation : <i>${repair} FCFA</i></li>
        <li>Loyer mensuel : <i>${rent} FCFA</i></li>
        <li>Nombre de mois : <i>${month}</i></li>
        <li>Mode de paiement : <i>${mode}</i></li>
    </ul>
    <p>Merci,</p>
    <p>L'équipe support</p>
</body>
</html>