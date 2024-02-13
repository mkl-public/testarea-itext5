using iTextSharp.text.pdf;
using iTextSharp.text;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace iText5.Net_Playground.Annotate
{
    internal class AddMultipleFileAttachments
    {
        /// <summary>
        /// itext failing to attach files when adding more than 60 attachments
        /// https://stackoverflow.com/questions/77988848/itext-failing-to-attach-files-when-adding-more-than-60-attachments
        ///
        /// This test attaches 73 file attachments to a PDF, works as expected, so cannot reproduce the issue of the OP.
        /// 
        /// (ported from the Java test with the same name)
        /// </summary>
        [Test]
        public void Add73Files()
        {
            var resultFile = @"..\..\..\target\test-outputs\annotate\73Attachments.pdf";
            Directory.CreateDirectory(@"..\..\..\target\test-outputs\annotate");

            using (Document document = new Document())
            {
                PdfWriter writer = PdfWriter.GetInstance(document, File.Create(resultFile));
                document.Open();

                document.Add(new Paragraph("Test file with 73 attachments"));

                for (int i = 0; i < 73; i++)
                {
                    PdfFileSpecification fs = PdfFileSpecification.FileEmbedded(writer,
                            String.Format("folder/file_{0}.txt", i),
                            String.Format("File {0}.txt", i),
                            Encoding.UTF8.GetBytes(String.Format("Contents of file {0}", i)));
                    fs.AddDescription("specificname", false);
                    writer.AddFileAttachment(fs);
                }

                document.Close();
            }
        }
    }
}
