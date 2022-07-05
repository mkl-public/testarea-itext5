using iTextSharp.text;
using iTextSharp.text.pdf;
using NUnit.Framework;
using System.IO;

namespace iText5.Net_Playground.Stamp
{
    class CutPages
    {
        /// <summary>
        /// iTextSharp: How do I cut off one half of a PDF page?
        /// https://stackoverflow.com/questions/72861974/itextsharp-how-do-i-cut-off-one-half-of-a-pdf-page
        /// new pdf1.pdf
        /// https://drive.google.com/file/d/1MPVn8P-S0olyR1MoT1T5fNLgxe6epQiS/view?usp=sharing
        /// 
        /// This test shows how to cut away the bottom half of the pages of a PDF.
        /// </summary>
        [Test]
        public void CutInHalfForTmighty()
        {
            var testFile = @"..\..\..\src\test\resources\mkl\testarea\itext5\stamp\new pdf1.pdf";
            var resultFile = @"..\..\..\target\test-outputs\stamp\new pdf1-Cut.pdf";
            Directory.CreateDirectory(@"..\..\..\target\test-outputs\stamp");

            using (PdfReader pdfReader = new PdfReader(testFile))
            using (PdfStamper pdfStamper = new PdfStamper(pdfReader, File.Create(resultFile)))
            {
                for (int i = 1; i <= pdfReader.NumberOfPages; i++)
                {
                    Rectangle cropBox = pdfReader.GetCropBox(i);
                    PdfArray newCropBox = new PdfArray(new float[] { cropBox.Left, (cropBox.Bottom + cropBox.Top) / 2, cropBox.Right, cropBox.Top });

                    PdfDictionary pageDictionary = pdfReader.GetPageN(i);
                    pageDictionary.Put(PdfName.CROPBOX, newCropBox);
                    pageDictionary.Put(PdfName.MEDIABOX, newCropBox);
                }
            }
        }
    }
}
