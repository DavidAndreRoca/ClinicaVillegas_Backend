package com.clinicavillegas.app.email;

public class MailTemplates {

    public static final String VERIFICATION_CODE = """
        <!DOCTYPE html>
        <html lang='es'>
        <head>
            <meta charset='UTF-8'>
            <meta name='viewport' content='width=device-width, initial-scale=1.0'>
            <title>Código de Verificación</title>
        </head>
        <body style='font-family: Arial, sans-serif; color: #333;'>
            <h2 style='color: #4CAF50;'>¡Hola!</h2>
            <p>Gracias por registrarte en Clínica Dental Villegas.</p>
            <p>Tu código de verificación es: <strong style='font-size: 1.2em;'>%s</strong></p>
            <p>Introduce este código en la página de verificación para completar tu registro.</p>
            <br>
            <p>Atentamente,</p>
            <p><strong>Clínica Dental Villegas</strong></p>
            </body>
        </html>""";


    public static final String USER_REMINDER = """
        <!DOCTYPE html>
        <html lang='es'>
        <head>
            <meta charset='UTF-8'>
            <meta name='viewport' content='width=device-width, initial-scale=1.0'>
            <title>Recordatorio de cita</title>
        </head>
        <body style='font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0;'>
            <div style='max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>
                <header style='background-color: #0F2650; color: white; text-align: center; padding: 10px 0;'>
                    <h1 style='margin: 0;'>Clínica Dental Villegas</h1>
                </header>
                <section style='padding: 20px;'>
                    <h2 style='color: #0F2650;'>Estimado(a) %s %s %s</h2>
                    <p>Gracias por confiar en <strong>Clínica Dental Villegas</strong>. A continuación, te recordamos los detalles de tu cita:</p>
                    <table style='width: 100%%; border-collapse: collapse; margin: 20px 0;'>
                        <tr style='background-color: #f9f9f9;'>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Fecha de la cita:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s</td>
                        </tr>
                        <tr>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Hora:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s</td>
                        </tr>
                        <tr style='background-color: #f9f9f9;'>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Tratamiento:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s</td>
                        </tr>
                        <tr>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Monto:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>S/ %s</td>
                        </tr>
                        <tr style='background-color: #f9f9f9;'>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Documento:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s - %s</td>
                        </tr>
                        <tr>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Dentista:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Dr. %s %s %s</td>
                        </tr>
                    </table>
                    <p style='font-size: 0.9em;'>Por favor, llega 10 minutos antes de la hora programada.</p>
                </section>
                <footer style='background-color: #f4f4f4; color: #666; text-align: center; padding: 10px;'>
                    <p style='margin: 0;'>© 2024 Clínica Dental Villegas. Todos los derechos reservados.</p>
                </footer>
            </div>
        </body>
        </html>""";


    public static final String DENTIST_REMINDER = """
        <!DOCTYPE html>
        <html lang='es'>
        <head>
            <meta charset='UTF-8'>
            <meta name='viewport' content='width=device-width, initial-scale=1.0'>
            <title>Recordatorio de cita</title>
        </head>
        <body style='font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0;'>
            <div style='max-width: 600px; margin: 20px auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>
                <header style='background-color: #0F2650; color: white; text-align: center; padding: 10px 0;'>
                    <h1 style='margin: 0;'>Clínica Dental Villegas</h1>
                </header>
                <section style='padding: 20px;'>
                    <h2 style='color: #0F2650;'>Hola Dr. %s %s %s</h2>
                    <p>Le recordamos que tiene una cita programada:</p>
                    <table style='width: 100%%; border-collapse: collapse; margin: 20px 0;'>
                        <tr style='background-color: #f9f9f9;'>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Fecha de la cita:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s</td>
                        </tr>
                        <tr>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Hora:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s</td>
                        </tr>
                        <tr style='background-color: #f9f9f9;'>
                            <td style='padding: 10px; border: 1px solid #ddd;'>Paciente:</td>
                            <td style='padding: 10px; border: 1px solid #ddd;'>%s %s %s</td>
                        </tr>
                    </table>
                </section>
                <footer style='background-color: #f4f4f4; color: #666; text-align: center; padding: 10px;'>
                    <p style='margin: 0;'>© 2024 Clínica Dental Villegas. Todos los derechos reservados.</p>
                </footer>
            </div>
        </body>
        </html>""";

    private MailTemplates() {}
}
