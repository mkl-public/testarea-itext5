using iTextSharp.text;
using iTextSharp.text.pdf;
using NUnit.Framework;
using System;
using System.IO;
using static iTextSharp.text.Font;

namespace iText5.Net_Playground.Stamp
{
    class ScalePages
    {
        /// <summary>
        /// Resize pdf page, set margins ans the right rotation
        /// https://stackoverflow.com/questions/71558846/resize-pdf-page-set-margins-ans-the-right-rotation
        /// 
        /// This test demonstrates how to scale all pages of a source document
        /// to a standard size (either portrait or landscape) using a PdfStamper.
        /// </summary>
        [Test]
        public void ScaleToA4ForMouhssine()
        {
            var testFile = @"..\..\..\target\test-outputs\stamp\DocWithDifferentPageSizes.pdf";
            var resultFile = @"..\..\..\target\test-outputs\stamp\DocWithDifferentPageSizes-Scaled.pdf";
            Directory.CreateDirectory(@"..\..\..\target\test-outputs\stamp");

            byte[] testData = createDocWithDifferentPageSizes();
            File.WriteAllBytes(testFile, testData);

            Rectangle targetSizePortrait = PageSize.A4;

            using (PdfReader pdfReader = new PdfReader(testFile))
            using (PdfStamper pdfStamper = new PdfStamper(pdfReader, File.Create(resultFile)))
            {
                for (int i = 1; i <= pdfReader.NumberOfPages; i++)
                {
                    Rectangle cropBox = pdfReader.GetCropBox(i);
                    bool portrait = cropBox.Height > cropBox.Width;
                    float scaleX = (portrait ? targetSizePortrait.Width : targetSizePortrait.Height) / cropBox.Width;
                    float scaleY = (portrait ? targetSizePortrait.Height : targetSizePortrait.Width) / cropBox.Height;
                    float scale = Math.Min(scaleX, scaleY);

                    PdfArray newCropBox = new PdfArray(new float[] { cropBox.Left * scale, cropBox.Bottom * scale, cropBox.Right * scale, cropBox.Top * scale });

                    PdfDictionary pageDictionary = pdfReader.GetPageN(i);
                    pageDictionary.Put(PdfName.CROPBOX, newCropBox);
                    pageDictionary.Put(PdfName.MEDIABOX, newCropBox);

                    ByteBuffer byteBuffer = new ByteBuffer();
                    byteBuffer.Append(scale).Append(' ')
                              .Append(0).Append(' ')
                              .Append(0).Append(' ')
                              .Append(scale).Append(' ')
                              .Append(0).Append(' ')
                              .Append(0).Append(' ')
                              .Append("cm\n");
                    byteBuffer.Append(PdfReader.GetPageContent(pageDictionary));

                    PdfStream stream = new PdfStream(byteBuffer.ToByteArray());
                    stream.FlateCompress(9);
                    pageDictionary.Put(PdfName.CONTENTS, pdfStamper.Writer.AddToBody(stream).IndirectReference);
                }
                pdfReader.RemoveUnusedObjects();
            }
        }

        byte[] createDocWithDifferentPageSizes()
        {
            string[] pageSizes = new string[] { "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7" };
            Font fontBig = new(FontFamily.HELVETICA, 40);
            Font fontSmall = new(FontFamily.HELVETICA, 10);
            using (MemoryStream ms = new MemoryStream())
            {
                using (Document document = new Document(PageSize.GetRectangle(pageSizes[0])))
                {
                    PdfWriter pdfWriter = PdfWriter.GetInstance(document, ms);
                    document.Open();

                    foreach (string pageSize in pageSizes)
                    {
                        Rectangle sizePortrait = PageSize.GetRectangle(pageSize);
                        AddPageWithSize(document, pdfWriter, sizePortrait, new string[] { pageSize, "Portrait" });

                        Rectangle sizeLandscape = new Rectangle(sizePortrait.Height, sizePortrait.Width);
                        AddPageWithSize(document, pdfWriter, sizeLandscape, new string[] { pageSize, "Landscape" });

                        Rectangle sizePortraitRot = sizePortrait.Rotate();
                        AddPageWithSize(document, pdfWriter, sizePortraitRot, new string[] { pageSize, "Landscape", "(Portrait rotated)" });
                    }
                }
                return ms.ToArray();
            }
        }

        void AddPageWithSize(Document document, PdfWriter pdfWriter, Rectangle rectangle, string[] description)
        {
            document.SetPageSize(rectangle);
            document.NewPage();
            document.Add(new Paragraph(new Chunk(description[0], fontBig)));
            for (int i = 1; i < description.Length; i++)
                document.Add(new Paragraph(new Chunk(description[i], fontSmall)));

            PdfContentByte contentUnder = pdfWriter.DirectContentUnder;
            contentUnder.SetLineWidth(4);
            contentUnder.SetRGBColorStroke(255, 0, 0);
            contentUnder.Rectangle(rectangle.Left + 2, rectangle.Bottom + 2, rectangle.Width - 4, rectangle.Height - 4);
            contentUnder.Stroke();
        }

        static Font fontBig = new(FontFamily.HELVETICA, 40);
        static Font fontSmall = new(FontFamily.HELVETICA, 10);
    }
}
