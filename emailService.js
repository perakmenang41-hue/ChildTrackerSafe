const sgMail = require('@sendgrid/mail');
sgMail.setApiKey(process.env.SENDGRID_API_KEY);

async function sendRegistrationEmail(toEmail, uniqueId) {
  const msg = {
    to: toEmail,
    from: process.env.FROM_EMAIL,
    subject: 'Your Child Tracker Unique ID',
    text: `Your unique ID: ${uniqueId}`,
    html: `<strong>Your unique ID: ${uniqueId}</strong>`
  };

  try {
    await sgMail.send(msg);
    console.log("Email sent");
  } catch (error) {
    console.error("SendGrid Error:", error.response?.body || error);
  }
}

module.exports = sendRegistrationEmail;
