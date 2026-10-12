-- Completa el contenido que V2 dejó como TODO(clinico): resumen para paciente, notas clínicas
-- cervicales y referencia bibliográfica de cada raíz. Pendiente de revisión clínica antes de
-- publicar (README, hoja de ruta, paso 5).
--
-- Fuentes:
--   [Lee 2008]      Lee MW, McPhee RW, Stringer MD. An evidence-based approach to human dermatomes.
--                   Clin Anat. 2008;21(5):363-373. doi:10.1002/ca.20636
--   [Bono 2011]     Bono CM, Ghiselli G, Gilbert TJ, et al. NASS. An evidence-based clinical guideline
--                   for the diagnosis and treatment of cervical radiculopathy from degenerative
--                   disorders. Spine J. 2011;11(1):64-72.
--   [Wainner 2003]  Wainner RS, Fritz JM, Irrgang JJ, et al. Reliability and diagnostic accuracy of the
--                   clinical examination and patient self-report measures for cervical radiculopathy.
--                   Spine. 2003;28(1):52-62.
--   [Rubinstein 2007] Rubinstein SM, Pool JJ, van Tulder MW, et al. A systematic review of the
--                   diagnostic accuracy of provocative tests of the neck for diagnosing cervical
--                   radiculopathy. Eur Spine J. 2007;16(3):307-319.
--   [Radhakrishnan 1994] Radhakrishnan K, Litchy WJ, O'Fallon WM, Kurland LT. Epidemiology of
--                   cervical radiculopathy. Brain. 1994;117(Pt 2):325-335.
--   [Kreiner 2014]  Kreiner DS, Hwang SW, Easa JE, et al. NASS. An evidence-based clinical guideline
--                   for the diagnosis and treatment of lumbar disc herniation with radiculopathy.
--                   Spine J. 2014;14(1):180-191.
--   [NICE NG59]     NICE. Low back pain and sciatica in over 16s: assessment and management (NG59).
--   [Devillé 2000]  Devillé WL, van der Windt DA, Dzaferagić A, et al. The test of Lasègue: systematic
--                   review of the accuracy in diagnosing herniated discs. Spine. 2000;25(9):1140-1147.

-- Resúmenes para paciente: lenguaje claro, "puede", nunca "tienes".

UPDATE nerve_root SET patient_summary =
    'La raíz C5 sale del cuello, entre las vértebras C4 y C5. Da sensibilidad a la cara externa del '
    || 'hombro y ayuda a separar el brazo del cuerpo y a doblar el codo. Cuando se irrita puede causar '
    || 'dolor u hormigueo en el hombro y dificultad para levantar el brazo.'
WHERE code = 'C5';

UPDATE nerve_root SET patient_summary =
    'La raíz C6 sale del cuello, entre las vértebras C5 y C6. Da sensibilidad al lado externo del '
    || 'antebrazo, al pulgar y al índice, y ayuda a extender la muñeca y doblar el codo. Cuando se irrita '
    || 'puede causar dolor que baja por el brazo hasta el pulgar, con hormigueo o adormecimiento.'
WHERE code = 'C6';

UPDATE nerve_root SET patient_summary =
    'La raíz C7 sale del cuello, entre las vértebras C6 y C7. Da sensibilidad al dedo medio y ayuda a '
    || 'estirar el codo y flexionar la muñeca. Cuando se irrita puede causar dolor que baja por la parte '
    || 'de atrás del brazo hasta el dedo medio, y menos fuerza al empujar.'
WHERE code = 'C7';

UPDATE nerve_root SET patient_summary =
    'La raíz C8 sale entre la última vértebra del cuello y la primera del tórax. Da sensibilidad al '
    || 'anular, al meñique y al lado interno del antebrazo, y ayuda a cerrar el puño con fuerza. Cuando se '
    || 'irrita puede causar hormigueo en esos dedos y dificultad para agarrar objetos.'
WHERE code = 'C8';

UPDATE nerve_root SET patient_summary =
    'La raíz T1 sale entre la primera y la segunda vértebra del tórax. Da sensibilidad al lado interno '
    || 'del brazo y ayuda a separar los dedos de la mano. Se afecta con poca frecuencia; cuando ocurre '
    || 'puede notarse menos fuerza o habilidad en la mano.'
WHERE code = 'T1';

UPDATE nerve_root SET patient_summary =
    'La raíz L1 sale de la parte alta de la columna lumbar. Da sensibilidad a la ingle y ayuda a levantar '
    || 'el muslo. Se afecta con poca frecuencia; el dolor en la ingle tiene muchas otras causas, como la '
    || 'cadera o una hernia inguinal, que un profesional debe descartar.'
WHERE code = 'L1';

UPDATE nerve_root SET patient_summary =
    'La raíz L2 sale de la parte alta de la columna lumbar. Da sensibilidad a la ingle y a la parte alta '
    || 'del muslo por delante, y ayuda a levantar el muslo y juntar las piernas. Cuando se irrita puede '
    || 'causar dolor u hormigueo en la ingle y el muslo.'
WHERE code = 'L2';

UPDATE nerve_root SET patient_summary =
    'La raíz L3 sale de la columna lumbar media. Da sensibilidad a la cara anterior del muslo y al lado '
    || 'interno de la rodilla, y ayuda a estirar la rodilla. Cuando se irrita puede causar dolor en el '
    || 'muslo por delante y sensación de que la rodilla falla.'
WHERE code = 'L3';

UPDATE nerve_root SET patient_summary =
    'La raíz L4 sale de la columna lumbar baja. Da sensibilidad al lado interno de la rodilla, de la '
    || 'pierna y del tobillo, y ayuda a estirar la rodilla y levantar la punta del pie. Cuando se irrita '
    || 'puede causar dolor que baja por el lado interno de la pierna.'
WHERE code = 'L4';

UPDATE nerve_root SET patient_summary =
    'La raíz L5 sale de la columna lumbar baja y es de las que se irritan con más frecuencia. Da '
    || 'sensibilidad al lado externo de la pierna, al empeine y al dedo gordo, y ayuda a levantar el dedo '
    || 'gordo y la punta del pie. Cuando se irrita puede causar dolor tipo ciática que llega al empeine, y '
    || 'a veces dificultad para caminar de talones.'
WHERE code = 'L5';

UPDATE nerve_root SET patient_summary =
    'La raíz S1 sale de la unión entre la columna lumbar y el sacro, y es de las que se irritan con más '
    || 'frecuencia. Da sensibilidad a la parte de atrás del muslo y la pantorrilla, al borde externo del pie '
    || 'y a la planta, y ayuda a pararse de puntas. Cuando se irrita puede causar dolor tipo ciática que '
    || 'llega a la planta o al borde externo del pie.'
WHERE code = 'S1';

-- Notas clínicas cervicales (V2 ya trae las lumbosacras del README).

UPDATE nerve_root SET clinical_notes =
    'Diferencial obligado con patología del manguito rotador y capsulitis: arco doloroso y pérdida de '
    || 'rotación pasiva orientan al hombro; dolor cervical asociado y parestesias, a la raíz. Reflejo '
    || 'bicipital compartido con C6. Spurling: específico, sensibilidad baja a moderada; un ULTT negativo '
    || 'ayuda a descartar radiculopatía. El cluster de Wainner (ULTT A, Spurling, distracción, rotación '
    || 'cervical < 60°) es más útil que cualquier prueba aislada.'
WHERE code = 'C5';

UPDATE nerve_root SET clinical_notes =
    'Segunda raíz cervical afectada con más frecuencia, después de C7. Diferencial con túnel carpiano: '
    || 'en el túnel carpiano se respetan la sensibilidad del antebrazo lateral, la extensión de muñeca y el '
    || 'reflejo braquiorradial. Spurling: específico, sensibilidad baja a moderada; un ULTT1 (mediano) '
    || 'negativo ayuda a descartar radiculopatía. Usar el cluster de Wainner mejor que pruebas aisladas.'
WHERE code = 'C6';

UPDATE nerve_root SET clinical_notes =
    'Raíz cervical afectada con más frecuencia. Reflejo tricipital disminuido y debilidad de extensión '
    || 'del codo orientan a C7. Spurling: específico, sensibilidad baja a moderada; un ULTT1 negativo ayuda '
    || 'a descartar radiculopatía. Usar el cluster de Wainner mejor que pruebas aisladas.'
WHERE code = 'C7';

UPDATE nerve_root SET clinical_notes =
    'Diferencial con neuropatía cubital (codo o canal de Guyon): la alteración sensitiva del antebrazo '
    || 'medial apunta a C8, porque el nervio cubital no inerva esa zona. Si hay síndrome de Horner o '
    || 'síntomas sistémicos, descartar tumor del vértice pulmonar (Pancoast).'
WHERE code = 'C8';

UPDATE nerve_root SET clinical_notes =
    'La radiculopatía T1 por hernia discal es poco frecuente. Debilidad de la musculatura intrínseca de la '
    || 'mano con síndrome de Horner obliga a descartar tumor del vértice pulmonar (Pancoast). Diferencial '
    || 'con síndrome del desfiladero torácico neurogénico.'
WHERE code = 'T1';

-- Referencias por raíz.

UPDATE nerve_root SET source_reference =
    'Bono 2011 (NASS, radiculopatía cervical); Wainner 2003 (Spine 28:52-62); '
    || 'Rubinstein 2007 (Eur Spine J 16:307-319); Lee 2008 (Clin Anat 21:363-373)'
WHERE code = 'C5';

UPDATE nerve_root SET source_reference =
    'Bono 2011 (NASS, radiculopatía cervical); Radhakrishnan 1994 (Brain 117:325-335); '
    || 'Wainner 2003 (Spine 28:52-62); Rubinstein 2007 (Eur Spine J 16:307-319); Lee 2008 (Clin Anat 21:363-373)'
WHERE code IN ('C6', 'C7');

UPDATE nerve_root SET source_reference =
    'Bono 2011 (NASS, radiculopatía cervical); Lee 2008 (Clin Anat 21:363-373)'
WHERE code IN ('C8', 'T1');

UPDATE nerve_root SET source_reference =
    'Kreiner 2014 (NASS, hernia discal lumbar con radiculopatía); NICE NG59; Lee 2008 (Clin Anat 21:363-373)'
WHERE code IN ('L1', 'L2', 'L3');

UPDATE nerve_root SET source_reference =
    'Kreiner 2014 (NASS, hernia discal lumbar con radiculopatía); NICE NG59; '
    || 'Devillé 2000 (Spine 25:1140-1147); Lee 2008 (Clin Anat 21:363-373)'
WHERE code IN ('L4', 'L5', 'S1');
